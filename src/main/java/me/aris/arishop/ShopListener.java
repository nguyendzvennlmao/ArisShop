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
            String command = p.hasMetadata("aris_command") ? p.getMetadata("aris_command").get(0).asString() : "";
            String shopFile = p.hasMetadata("aris_shop_file") ? p.getMetadata("aris_shop_file").get(0).asString() : "";

            int slot = e.getSlot();

            if (slot == gui.getInt("confirm.slot")) {
                double total = price * currentAmount;
                boolean canBuy = false;

                if (currency.equalsIgnoreCase("SHARDS")) {
                    int balance = 0;
                    try {
                        String rawBal = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, m.getConfig().getString("currencies.shards.balance-placeholder"));
                        balance = Integer.parseInt(rawBal.replaceAll("[^0-9]", ""));
                    } catch (Exception ex) { balance = 0; }

                    if (balance >= total) {
                        String takeCmd = m.getConfig().getString("currencies.shards.take-command").replace("%player%", p.getName()).replace("%price%", String.valueOf((int)total));
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), takeCmd);
                        canBuy = true;
                    } else {
                        p.sendMessage(m.color(m.getConfig().getString("messages.prefix") + m.getConfig().getString("messages.insufficient-shards")));
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                    }
                } else {
                    if (ArisShop.getEconomy().getBalance(p) >= total) {
                        ArisShop.getEconomy().withdrawPlayer(p, total);
                        canBuy = true;
                    } else {
                        p.sendMessage(m.color(m.getConfig().getString("messages.prefix") + m.getConfig().getString("messages.insufficient-funds")));
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                    }
                }

                if (canBuy) {
                    if (!command.isEmpty()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", p.getName()).replace("%amount%", String.valueOf(currentAmount)));
                    } else {
                        if (p.getInventory().firstEmpty() == -1) {
                            p.sendMessage(m.color(m.getConfig().getString("messages.prefix") + m.getConfig().getString("messages.full-inventory.text")));
                            p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                            return;
                        }
                        ItemStack item = shopItem.clone();
                        item.setAmount(currentAmount);
                        p.getInventory().addItem(item);
                    }
                    String msg = m.getConfig().getString("messages.buy-success.text").replace("%amount%", String.valueOf(currentAmount)).replace("%item%", shopItem.getItemMeta().getDisplayName());
                    p.sendMessage(m.color(m.getConfig().getString("messages.prefix") + msg));
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-success")), 1, 1);
                }
            } else if (slot == gui.getInt("cancel.slot")) {
                p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.cancel-click")), 1, 1);
                if (!shopFile.isEmpty()) ShopItem.open(p, shopFile); else p.closeInventory();
            } else {
                int change = 0;
                if (slot == gui.getInt("add1.slot")) change = 1;
                else if (slot == gui.getInt("add10.slot")) change = 10;
                else if (slot == gui.getInt("set64.slot")) { currentAmount = 64; change = 0; }
                else if (slot == gui.getInt("remove1.slot")) change = -1;
                else if (slot == gui.getInt("remove10.slot")) change = -10;
                else if (slot == gui.getInt("remove64.slot")) { currentAmount = 1; change = 0; }
                int newAmount = Math.min(64, Math.max(1, currentAmount + change));
                if (newAmount != currentAmount || slot == gui.getInt("set64.slot") || slot == gui.getInt("remove64.slot")) {
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                    ConfirmPurchase.open(p, price, shopItem, newAmount);
                }
            }
            return;
        }
        handleShop(p, title, e.getSlot(), e.getCurrentItem(), e);
    }

    private void handleShop(Player p, String title, int slot, ItemStack item, InventoryClickEvent e) {
        ArisShop m = ArisShop.getInstance();
        File folder = new File(m.getDataFolder(), "shop");
        if (!folder.exists()) return;
        for (File f : folder.listFiles()) {
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            if (title.equals(m.color(c.getString("title")))) {
                e.setCancelled(true);
                ConfigurationSection sec = c.getConfigurationSection("items");
                for (String k : sec.getKeys(false)) {
                    if (slot == sec.getInt(k + ".slot")) {
                        p.setMetadata("aris_currency", new FixedMetadataValue(m, c.getString("currency", "MONEY")));
                        p.setMetadata("aris_command", new FixedMetadataValue(m, sec.getString(k + ".command", "")));
                        p.setMetadata("aris_shop_file", new FixedMetadataValue(m, f.getName().replace(".yml", "")));
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                        ConfirmPurchase.open(p, sec.getDouble(k + ".price"), item, 1);
                        return;
                    }
                }
            }
        }
    }
            }
