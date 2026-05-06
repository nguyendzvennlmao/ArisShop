package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.ArrayList;
import java.util.List;

public class ConfirmPurchase implements Listener {
    
    private static ArisShop plugin;
    
    public ConfirmPurchase(ArisShop plugin) {
        ConfirmPurchase.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public static void open(Player p, double price, ItemStack shopItem, int amount, int maxStack) {
        ConfigurationSection gui = plugin.getConfig().getConfigurationSection("gui.quantity-selector");
        Inventory inv = Bukkit.createInventory(null, gui.getInt("rows", 3) * 9, plugin.color(gui.getString("title")));
        
        p.setMetadata("aris_price", new FixedMetadataValue(plugin, price));
        p.setMetadata("aris_amount", new FixedMetadataValue(plugin, amount));
        p.setMetadata("aris_item", new FixedMetadataValue(plugin, shopItem));
        p.setMetadata("aris_stack", new FixedMetadataValue(plugin, maxStack));
        
        addButton(inv, gui, "confirm");
        addButton(inv, gui, "cancel");
        
        if (maxStack == 1) {
        }
        else if (maxStack == 16) {
            if (amount < 16) {
                addButton(inv, gui, "add1");
                if (amount + 10 <= 16) {
                    addButton(inv, gui, "add10");
                }
            }
            if (amount > 1) {
                addButton(inv, gui, "remove1");
            }
            if (amount > 10) {
                addButton(inv, gui, "remove10");
            }
        }
        else if (maxStack == 64) {
            if (amount < 64) {
                addButton(inv, gui, "add1");
                if (amount + 10 <= 64) {
                    addButton(inv, gui, "add10");
                }
                if (amount + 64 <= 64) {
                    addButton(inv, gui, "add64");
                }
            }
            if (amount > 1) {
                addButton(inv, gui, "remove1");
            }
            if (amount > 10) {
                addButton(inv, gui, "remove10");
            }
            if (amount >= 64) {
                addButton(inv, gui, "remove64");
            }
        }
        
        ItemStack preview = shopItem.clone();
        preview.setAmount(amount);
        ItemMeta pMeta = preview.getItemMeta();
        if (pMeta != null) {
            List<String> lore = new ArrayList<>();
            String priceFormatted = plugin.format(price);
            String totalFormatted = plugin.format(price * amount);
            for (String s : gui.getStringList("item-preview.lore")) {
                lore.add(plugin.color(s.replace("%price%", priceFormatted)
                                 .replace("%amount%", String.valueOf(amount))
                                 .replace("%total_price%", totalFormatted)));
            }
            pMeta.setLore(lore);
            preview.setItemMeta(pMeta);
        }
        inv.setItem(gui.getInt("item-preview.slot"), preview);
        
        p.openInventory(inv);
    }
    
    private static void addButton(Inventory inv, ConfigurationSection gui, String buttonPath) {
        ConfigurationSection sec = gui.getConfigurationSection(buttonPath);
        if (sec != null) {
            try {
                ItemStack item = new ItemStack(Material.valueOf(sec.getString("material")));
                ItemMeta meta = item.getItemMeta();
                if (meta != null && sec.contains("name")) {
                    meta.setDisplayName(plugin.color(sec.getString("name")));
                }
                item.setItemMeta(meta);
                inv.setItem(sec.getInt("slot"), item);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid material for button: " + buttonPath);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player p = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        ConfigurationSection gui = plugin.getConfig().getConfigurationSection("gui.quantity-selector");
        if (title == null || !title.equals(plugin.color(gui.getString("title")))) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (!p.hasMetadata("aris_price") || !p.hasMetadata("aris_amount") || 
            !p.hasMetadata("aris_item") || !p.hasMetadata("aris_stack")) return;
        
        double price = p.getMetadata("aris_price").get(0).asDouble();
        int currentAmount = p.getMetadata("aris_amount").get(0).asInt();
        ItemStack shopItem = (ItemStack) p.getMetadata("aris_item").get(0).value();
        int maxStack = p.getMetadata("aris_stack").get(0).asInt();
        String curr = p.hasMetadata("aris_curr") ? p.getMetadata("aris_curr").get(0).asString() : "MONEY";
        String file = p.hasMetadata("aris_file") ? p.getMetadata("aris_file").get(0).asString() : "";
        
        String displayName = clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "";
        
        if (displayName.contains(plugin.color(gui.getString("confirm.name")))) {
            double totalPrice = price * currentAmount;
            
            boolean can = false;
            if (curr.equalsIgnoreCase("SHARDS")) {
                double bal = 0;
                try {
                    String raw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, plugin.getConfig().getString("currencies.shards.balance-placeholder"));
                    bal = Double.parseDouble(raw.replaceAll("[^0-9.]", ""));
                } catch (Exception ex) { bal = 0; }
                
                if (bal >= totalPrice) {
                    plugin.runTask(p, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                        plugin.getConfig().getString("currencies.shards.take-command")
                            .replace("%player%", p.getName())
                            .replace("%price%", String.valueOf((long)totalPrice))));
                    can = true;
                } else {
                    plugin.sendMsg(p, "insufficient-shards");
                    p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.purchase-fail")), 1, 1);
                }
            } else {
                if (ArisShop.getEconomy().has(p, totalPrice)) {
                    ArisShop.getEconomy().withdrawPlayer(p, totalPrice);
                    can = true;
                } else {
                    plugin.sendMsg(p, "insufficient-funds");
                    p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.purchase-fail")), 1, 1);
                }
            }
            
            if (can) {
                if (p.getInventory().firstEmpty() == -1) {
                    plugin.sendMsg(p, "full-inventory");
                    p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.purchase-fail")), 1, 1);
                    return;
                }
                ItemStack toGive = shopItem.clone();
                toGive.setAmount(currentAmount);
                p.getInventory().addItem(toGive);
                String itemName = shopItem.getItemMeta() != null ? shopItem.getItemMeta().getDisplayName() : shopItem.getType().toString();
                plugin.sendMsg(p, "buy-success", "%amount%", String.valueOf(currentAmount), "%item%", itemName);
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.purchase-success")), 1, 1);
                p.closeInventory();
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("cancel.name")))) {
            p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.cancel-click")), 1, 1);
            p.closeInventory();
            if (!file.isEmpty()) {
                ShopItem.open(p, file);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("add1.name")))) {
            int newAmount = Math.min(currentAmount + 1, maxStack);
            if (newAmount != currentAmount) {
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.button-click")), 1, 1);
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("add10.name")))) {
            int newAmount = Math.min(currentAmount + 10, maxStack);
            if (newAmount != currentAmount) {
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.button-click")), 1, 1);
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("add64.name")))) {
            int newAmount = Math.min(currentAmount + 64, maxStack);
            if (newAmount != currentAmount) {
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.button-click")), 1, 1);
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("remove1.name")))) {
            int newAmount = Math.max(currentAmount - 1, 1);
            if (newAmount != currentAmount) {
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.button-click")), 1, 1);
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("remove10.name")))) {
            int newAmount = Math.max(currentAmount - 10, 1);
            if (newAmount != currentAmount) {
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.button-click")), 1, 1);
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("remove64.name")))) {
            int newAmount = 1;
            if (newAmount != currentAmount) {
                p.playSound(p.getLocation(), Sound.valueOf(plugin.getConfig().getString("sounds.button-click")), 1, 1);
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
    }
                                               }
