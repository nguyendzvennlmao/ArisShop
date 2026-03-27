package me.aris.arishop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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
            int slot = e.getSlot();

            if (slot == gui.getInt("confirm.slot")) {
                double total = price * currentAmount;
                if (ArisShop.getEconomy().getBalance(p) >= total) {
                    ArisShop.getEconomy().withdrawPlayer(p, total);
                    ItemStack finalItem = shopItem.clone();
                    finalItem.setAmount(currentAmount);
                    p.getInventory().addItem(finalItem);
                    p.sendMessage("§aMua thành công " + currentAmount + " món với giá " + total);
                } else { p.sendMessage("§cKhông đủ tiền!"); }
                p.closeInventory();
            } else if (slot == gui.getInt("cancel.slot")) {
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
                if (newAmount != currentAmount) ConfirmPurchase.open(p, price, shopItem, newAmount);
            }
            return;
        }

        File[] files = new File(m.getDataFolder(), "shop").listFiles();
        if (files != null) {
            for (File f : files) {
                YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
                ConfigurationSection d = c;
                if (c.getKeys(false).size() == 1 && !c.contains("items"))
                    d = c.getConfigurationSection(c.getKeys(false).iterator().next());
                if (title.equals(m.color(d.getString("title")))) {
                    e.setCancelled(true);
                    ConfigurationSection items = d.getConfigurationSection("items");
                    for (String k : items.getKeys(false)) {
                        if (e.getSlot() == items.getInt(k + ".slot")) {
                            ConfirmPurchase.open(p, items.getDouble(k + ".price"), e.getCurrentItem(), 1);
                            return;
                        }
                    }
                }
            }
        }
    }
                }
