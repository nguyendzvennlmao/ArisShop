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
            double price = p.getMetadata("aris_price").get(0).asDouble();
            int amount = p.getMetadata("aris_amount").get(0).asInt();
            int maxStack = p.getMetadata("aris_stack").get(0).asInt();
            ItemStack itemObj = (ItemStack) p.getMetadata("aris_item").get(0).value();
            String curr = p.hasMetadata("aris_curr") ? p.getMetadata("aris_curr").get(0).asString() : "MONEY";
            String cmd = p.hasMetadata("aris_cmd") ? p.getMetadata("aris_cmd").get(0).asString() : "";
            String file = p.hasMetadata("aris_file") ? p.getMetadata("aris_file").get(0).asString() : "";

            if (e.getSlot() == gui.getInt("confirm.slot")) {
                final double finalTotal = price * amount;
                final String finalCmd = cmd;
                final int finalAmount = amount;
                final ItemStack finalItemObj = itemObj;

                boolean can = false;
                if (curr.equalsIgnoreCase("SHARDS")) {
                    int bal = 0;
                    try {
                        String raw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, m.getConfig().getString("currencies.shards.balance-placeholder"));
                        bal = Integer.parseInt(raw.replaceAll("[^0-9]", ""));
                    } catch (Exception ex) { bal = 0; }
                    if (bal >= finalTotal) {
                        m.runTask(p, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), m.getConfig().getString("currencies.shards.take-command").replace("%player%", p.getName()).replace("%price%", String.valueOf((int)finalTotal))));
                        can = true;
                    } else {
                        m.sendMsg(p, "insufficient-shards");
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                    }
                } else {
                    if (ArisShop.getEconomy().getBalance(p) >= finalTotal) {
                        ArisShop.getEconomy().withdrawPlayer(p, finalTotal);
                        can = true;
                    } else {
                        m.sendMsg(p, "insufficient-funds");
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                    }
                }
                if (can) {
                    if (!finalCmd.isEmpty()) m.runTask(p, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd.replace("%player%", p.getName()).replace("%amount%", String.valueOf(finalAmount))));
                    else {
                        if (p.getInventory().firstEmpty() == -1) {
                            m.sendMsg(p, "full-inventory");
                            p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-fail")), 1, 1);
                            return;
                        }
                        ItemStack finalI = finalItemObj.clone();
                        finalI.setAmount(finalAmount);
                        p.getInventory().addItem(finalI);
                    }
                    m.sendMsg(p, "buy-success", "%amount%", String.valueOf(finalAmount), "%item%", finalItemObj.getItemMeta().getDisplayName());
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.purchase-success")), 1, 1);
                }
            } else if (e.getSlot() == gui.getInt("cancel.slot")) {
                p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.cancel-click")), 1, 1);
                ShopItem.open(p, file);
            } else {
                int add = 0;
                if (e.getSlot() == gui.getInt("add1.slot")) add = 1;
                else if (e.getSlot() == gui.getInt("add10.slot")) add = 10;
                else if (e.getSlot() == gui.getInt("set64.slot")) { amount = maxStack; add = 0; }
                else if (e.getSlot() == gui.getInt("remove1.slot")) add = -1;
                else if (e.getSlot() == gui.getInt("remove10.slot")) add = -10;
                else if (e.getSlot() == gui.getInt("remove64.slot")) { amount = 1; add = 0; }
                
                int newA = Math.min(maxStack, Math.max(1, amount + add));
                if (newA != amount || e.getSlot() == gui.getInt("set64.slot") || e.getSlot() == gui.getInt("remove64.slot")) {
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                    ConfirmPurchase.open(p, price, itemObj, newA, maxStack);
                }
            }
            return;
        }

        File folder = new File(m.getDataFolder(), "shop");
        if (folder.exists() && folder.listFiles() != null) {
            for (File f : folder.listFiles()) {
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
                            p.setMetadata("aris_cmd", new FixedMetadataValue(m, items.getString(k + ".command", "")));
                            p.setMetadata("aris_file", new FixedMetadataValue(m, f.getName().replace(".yml", "")));
                            p.setMetadata("aris_stack", new FixedMetadataValue(m, items.getInt(k + ".stack", 64)));
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
