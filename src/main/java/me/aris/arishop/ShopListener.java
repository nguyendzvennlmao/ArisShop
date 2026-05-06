package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import java.io.File;

public class ShopListener implements Listener {
    
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;
        ArisShop m = ArisShop.getInstance();
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.equals(m.color(m.getConfig().getString("main-menu.title")))) {
            e.setCancelled(true);
            ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
            for (String k : sec.getKeys(false)) {
                if (e.getSlot() == sec.getInt(k + ".slot")) {
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                    ShopItem.open(p, k);
                    return;
                }
            }
        }

        ConfigurationSection gui = m.getConfig().getConfigurationSection("gui.quantity-selector");
        if (title.equals(m.color(gui.getString("title")))) {
            e.setCancelled(true);
            
            if (!p.hasMetadata("aris_price") || !p.hasMetadata("aris_amount") || 
                !p.hasMetadata("aris_item") || !p.hasMetadata("aris_stack")) return;
            
            double price = p.getMetadata("aris_price").get(0).asDouble();
            int amount = p.getMetadata("aris_amount").get(0).asInt();
            int maxStack = p.getMetadata("aris_stack").get(0).asInt();
            ItemStack itemObj = (ItemStack) p.getMetadata("aris_item").get(0).value();
            String curr = p.hasMetadata("aris_curr") ? p.getMetadata("aris_curr").get(0).asString() : "MONEY";
            String file = p.hasMetadata("aris_file") ? p.getMetadata("aris_file").get(0).asString() : "";

            if (e.getSlot() == gui.getInt("confirm.slot")) {
                double totalPrice = price * amount;
                
                boolean can = false;
                if (curr.equalsIgnoreCase("SHARDS")) {
                    double bal = 0;
                    try {
                        String raw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, m.getConfig().getString("currencies.shards.balance-placeholder"));
                        bal = Double.parseDouble(raw.replaceAll("[^0-9.]", ""));
                    } catch (Exception ex) { bal = 0; }
                    
                    if (bal >= totalPrice) {
                        m.runTask(p, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                            m.getConfig().getString("currencies.shards.take-command")
                                .replace("%player%", p.getName())
                                .replace("%price%", String.valueOf((long)totalPrice))));
                        can = true;
                    } else {
                        m.sendMsg(p, "insufficient-shards");
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                    }
                } else {
                    if (ArisShop.getEconomy().getBalance(p) >= totalPrice) {
                        ArisShop.getEconomy().withdrawPlayer(p, totalPrice);
                        can = true;
                    } else {
                        m.sendMsg(p, "insufficient-funds");
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                    }
                }

                if (can) {
                    if (p.getInventory().firstEmpty() == -1) {
                        m.sendMsg(p, "full-inventory");
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                        return;
                    }
                    ItemStack finalI = itemObj.clone();
                    finalI.setAmount(amount);
                    p.getInventory().addItem(finalI);
                    String itemName = itemObj.getItemMeta() != null ? itemObj.getItemMeta().getDisplayName() : itemObj.getType().toString();
                    m.sendMsg(p, "buy-success", "%amount%", String.valueOf(amount), "%item%", itemName);
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-success")), 1, 1);
                    p.closeInventory();
                }
            } 
            else if (e.getSlot() == gui.getInt("cancel.slot")) {
                p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.cancel-click")), 1, 1);
                p.closeInventory();
                ShopItem.open(p, file);
            }
            return;
        }

        File folder = new File(m.getDataFolder(), "shop");
        if (folder.exists() && folder.listFiles() != null) {
            for (File f : folder.listFiles()) {
                if (f == null || !f.getName().endsWith(".yml")) continue;
                YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
                if (title.equals(m.color(c.getString("title")))) {
                    e.setCancelled(true);
                    if (e.getSlot() == m.getConfig().getInt("gui.back-button.slot", 22)) {
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                        ShopMain.open(p);
                        return;
                    }
                    ConfigurationSection items = c.getConfigurationSection("items");
                    if (items == null) return;
                    for (String k : items.getKeys(false)) {
                        if (e.getSlot() == items.getInt(k + ".slot")) {
                            p.setMetadata("aris_curr", new FixedMetadataValue(m, c.getString("currency", "MONEY")));
                            p.setMetadata("aris_file", new FixedMetadataValue(m, f.getName().replace(".yml", "")));
                            p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                            ConfirmPurchase.open(p, items.getDouble(k + ".price"), e.getCurrentItem(), 1, items.getInt(k + ".stack", 64));
                            return;
                        }
                    }
                }
            }
        }
    }
                        }
