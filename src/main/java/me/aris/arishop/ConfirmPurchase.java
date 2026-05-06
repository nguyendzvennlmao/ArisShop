package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    
    private ArisShop plugin;
    
    public ConfirmPurchase(ArisShop plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public void open(Player p, double price, ItemStack shopItem, int amount, int maxStack) {
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
            if (amount == 64) {
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
    
    private void addButton(Inventory inv, ConfigurationSection gui, String buttonPath) {
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
        
        String displayName = clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "";
        
        if (displayName.contains(plugin.color(gui.getString("confirm.name")))) {
            double totalPrice = price * currentAmount;
            if (ArisShop.getEconomy().has(p, totalPrice)) {
                ArisShop.getEconomy().withdrawPlayer(p, totalPrice);
                ItemStack toGive = shopItem.clone();
                toGive.setAmount(currentAmount);
                p.getInventory().addItem(toGive);
                p.closeInventory();
                plugin.sendMsg(p, "buy-success", "%amount%", String.valueOf(currentAmount), "%item%", shopItem.getType().toString());
            } else {
                plugin.sendMsg(p, "insufficient-funds");
                p.closeInventory();
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("cancel.name")))) {
            p.closeInventory();
        }
        else if (displayName.contains(plugin.color(gui.getString("add1.name")))) {
            int newAmount = Math.min(currentAmount + 1, maxStack);
            if (newAmount != currentAmount) {
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("add10.name")))) {
            int newAmount = Math.min(currentAmount + 10, maxStack);
            if (newAmount != currentAmount) {
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("add64.name")))) {
            int newAmount = Math.min(currentAmount + 64, maxStack);
            if (newAmount != currentAmount) {
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("remove1.name")))) {
            int newAmount = Math.max(currentAmount - 1, 1);
            if (newAmount != currentAmount) {
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("remove10.name")))) {
            int newAmount = Math.max(currentAmount - 10, 17);
            if (newAmount < 1) newAmount = 1;
            if (newAmount != currentAmount) {
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
        else if (displayName.contains(plugin.color(gui.getString("remove64.name")))) {
            int newAmount = 1;
            if (newAmount != currentAmount) {
                open(p, price, shopItem, newAmount, maxStack);
            }
        }
    }
                         }
