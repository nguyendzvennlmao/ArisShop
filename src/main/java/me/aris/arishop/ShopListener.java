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
            p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
            ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
            for (String key : sec.getKeys(false)) {
                if (e.getSlot() == sec.getInt(key + ".slot")) { ShopItem.open(p, key); return; }
            }
        }

        ConfigurationSection gui = m.getConfig().getConfigurationSection("gui.quantity-selector");
        if (title.equals(m.color(gui.getString("title")))) {
            e.setCancelled(true);
            double price = p.getMetadata("aris_price").get(0).asDouble();
            int currentAmount = p.getMetadata("aris_amount").get(0).asInt();
            ItemStack shopItem = (ItemStack) p.getMetadata("aris_item").get(0).value();
            String currency = p.hasMetadata("aris_currency") ? p.getMetadata("aris_currency").get(0).asString() : "MONEY";
            String command = p.hasMetadata("aris_command") ? p.getMetadata("aris_command").get(0).asString() : null;

            int slot = e.getSlot();

            if (slot == gui.getInt("confirm.slot")) {
                double total = price * currentAmount;
                boolean success = false;
                if (currency.equalsIgnoreCase("SHARDS")) {
                    String checkCmd = m.getConfig().getString("currencies.shards.take-command").replace("%player%", p.getName()).replace("%price%", String.valueOf((int)total));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), checkCmd);
                    success = true; 
                } else {
                    if (ArisShop.getEconomy().getBalance(p) >= total) {
                        ArisShop.getEconomy().withdrawPlayer(p, total);
                        success = true;
                    } else {
                        p.sendMessage(m.color(m.getConfig().getString("messages.insufficient-funds")));
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                    }
                }
                if (success) {
                    if (command != null && !command.isEmpty()) {
                        String finalCmd = command.replace("%player%", p.getName()).replace("%amount%", String.valueOf(currentAmount));
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                    } else {
                        if (p.getInventory().firstEmpty() == -1) {
                            p.sendMessage(m.color(m.getConfig().getString("messages.full-inventory")));
                            p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                            return;
                        }
                        ItemStack finalItem = shopItem.clone();
                        finalItem.setAmount(currentAmount);
                        p.getInventory().addItem(finalItem);
                    }
                    p.sendMessage(m.color(m.getConfig().getString("messages.buy-success").replace("%amount%", String.valueOf(currentAmount)).replace("%item%", shopItem.getItemMeta().getDisplayName())));
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-success")), 1, 1);
                }
            } else if (slot == gui.getInt("cancel.slot")) {
                p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.cancel-click")), 1, 1);
                p.closeInventory();
            } else {
                int newAmount = currentAmount;
                if (slot == gui.getInt("add1.slot")) newAmount += 1;
                else if (slot == gui.getInt("add10.slot")) newAmount += 10;
                else if (slot == gui.getInt("set64.slot")) newAmount = 64;
                else if (slot == gui.getInt("remove1.slot")) newAmount -= 1;
                else if (slot == gui.getInt("remove10.slot")) newAmount -= 10;
                else if (slot == gui.getInt("remove64.slot")) newAmount = 1;
                if (newAmount < 1) newAmount = 1;
                if (newAmount > 64) newAmount = 64;
                if (newAmount != currentAmount) {
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                    ConfirmPurchase.open(p, price, shopItem, newAmount);
                }
            }
            return;
        }

        handleShopClick(p, title, e.getSlot(), e.getCurrentItem());
    }

    private void handleShopClick(Player p, String title, int slot, ItemStack clickedItem) {
        ArisShop m = ArisShop.getInstance();
        File folder = new File(m.getDataFolder(), "shop");
        if (!folder.exists()) return;
        for (File f : folder.listFiles()) {
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            ConfigurationSection d = c;
            if (c.getKeys(false).size() == 1 && !c.contains("items")) d = c.getConfigurationSection(c.getKeys(false).iterator().next());
            if (title.equals(m.color(d.getString("title")))) {
                e.setCancelled(true);
                ConfigurationSection items = d.getConfigurationSection("items");
                for (String k : items.getKeys(false)) {
                    if (slot == items.getInt(k + ".slot")) {
                        p.setMetadata("aris_currency", new FixedMetadataValue(m, d.getString("currency", "MONEY")));
                        p.setMetadata("aris_command", new FixedMetadataValue(m, items.getString(k + ".command", "")));
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                        ConfirmPurchase.open(p, items.getDouble(k + ".price"), clickedItem, 1);
                        return;
                    }
                }
            }
        }
    }
                }
