package me.aris.arishop;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {
    private final Map<UUID, ShopContext> sessions = new HashMap<>();
    public static class ShopContext { String cat, item, name, curr; int amount = 1; double price; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        ArisShop m = ArisShop.getInstance();
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.equals(m.color(m.getConfig().getString("main-menu.title")))) {
            e.setCancelled(true);
            ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
            for (String k : sec.getKeys(false)) {
                if (e.getSlot() == sec.getInt(k + ".slot")) {
                    ShopItem.open(p, k);
                    return;
                }
            }
            return;
        }

        File folder = new File(m.getDataFolder(), "shop");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
                if (title.equals(m.color(c.getString("title")))) {
                    e.setCancelled(true);
                    handleShopClick(p, f, e.getSlot());
                    return;
                }
            }
        }

        if (title.equals(m.color(m.getConfig().getString("gui.quantity-selector.title")))) {
            e.setCancelled(true);
            handleConfirm(p, e.getSlot(), e.getInventory());
        }
    }

    private void handleShopClick(Player p, File f, int slot) {
        ArisShop m = ArisShop.getInstance();
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection items = c.getConfigurationSection("items");
        if (items == null) return;
        for (String k : items.getKeys(false)) {
            if (slot == items.getInt(k + ".slot")) {
                ShopContext ctx = new ShopContext();
                ctx.cat = f.getName().replace(".yml", "");
                ctx.item = k;
                ctx.price = items.getDouble(k + ".price");
                ctx.name = m.color(items.getString(k + ".displayname"));
                ctx.curr = c.getString("currency", "VAULT");
                sessions.put(p.getUniqueId(), ctx);
                ConfirmPurchase.open(p, ctx);
                return;
            }
        }
    }

    private void handleConfirm(Player p, int s, org.bukkit.inventory.Inventory inv) {
        ArisShop m = ArisShop.getInstance();
        ShopContext ctx = sessions.get(p.getUniqueId());
        if (ctx == null) return;
        ConfigurationSection gui = m.getConfig().getConfigurationSection("gui.quantity-selector");
        if (s == gui.getInt("confirm.slot")) {
            double t = ctx.price * ctx.amount;
            if (ctx.curr != null && ctx.curr.equalsIgnoreCase("SHARDS")) {
                double b = Double.parseDouble(PlaceholderAPI.setPlaceholders(p, m.getConfig().getString("currencies.shards.balance-placeholder")));
                if (b < t) return;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), m.getConfig().getString("currencies.shards.take-command").replace("%player%", p.getName()).replace("%price%", String.valueOf((int)t)));
            } else {
                if (ArisShop.getEconomy().getBalance(p) < t) return;
                ArisShop.getEconomy().withdrawPlayer(p, t);
            }
            File f = new File(m.getDataFolder() + "/shop", ctx.cat + ".yml");
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            p.getInventory().addItem(new ItemStack(Material.valueOf(c.getString("items." + ctx.item + ".material")), ctx.amount));
            p.closeInventory();
        } else if (s == gui.getInt("cancel.slot")) ShopItem.open(p, ctx.cat);
        else {
            if (s == gui.getInt("add1.slot")) ctx.amount = Math.min(64, ctx.amount + 1);
            else if (s == gui.getInt("add10.slot")) ctx.amount = Math.min(64, ctx.amount + 10);
            else if (s == gui.getInt("set64.slot")) ctx.amount = 64;
            else if (s == gui.getInt("remove1.slot")) ctx.amount = Math.max(1, ctx.amount - 1);
            else if (s == gui.getInt("remove10.slot")) ctx.amount = Math.max(1, ctx.amount - 10);
            else if (s == gui.getInt("remove64.slot")) ctx.amount = 1;
            ConfirmPurchase.update(inv, ctx);
        }
    }
                        }
