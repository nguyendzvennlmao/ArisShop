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

    public static class ShopContext {
        String cat, item, name, curr;
        int amount = 1;
        double price;
    }

    @EventHandler
    public void on(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof GuiManager.MenuHolder holder) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            if (e.getCurrentItem() == null) return;

            if (holder.type.equals("MAIN")) {
                ConfigurationSection sec = ArisShop.getInstance().getConfig().getConfigurationSection("main-menu.categories");
                for (String k : sec.getKeys(false)) {
                    if (e.getSlot() == sec.getInt(k + ".slot")) {
                        GuiManager.openCategory(p, k);
                        return;
                    }
                }
            } else if (holder.type.equals("SHOP")) {
                handleShop(p, holder.id, e.getSlot());
            } else if (holder.type.equals("CONFIRM")) {
                handleConfirm(p, e.getSlot(), e.getInventory());
            }
        }
    }

    private void handleShop(Player p, String id, int s) {
        ArisShop m = ArisShop.getInstance();
        File f = new File(m.getDataFolder() + "/shop", id + ".yml");
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection items = c.getConfigurationSection("items");
        for (String k : items.getKeys(false)) {
            if (s == items.getInt(k + ".slot")) {
                ShopContext ctx = new ShopContext();
                ctx.cat = id;
                ctx.item = k;
                ctx.price = items.getDouble(k + ".price");
                ctx.name = m.color(items.getString(k + ".displayname"));
                ctx.curr = c.getString("currency", "VAULT");
                sessions.put(p.getUniqueId(), ctx);
                GuiManager.openConfirm(p, ctx);
                return;
            }
        }
    }

    private void handleConfirm(Player p, int s, org.bukkit.inventory.Inventory inv) {
        ArisShop m = ArisShop.getInstance();
        ShopContext ctx = sessions.get(p.getUniqueId());
        if (ctx == null) return;
        ConfigurationSection gui = m.getConfig().getConfigurationSection("gui.quantity-selector");

        if (s == gui.getInt("confirm.slot")) { buy(p, ctx); p.closeInventory(); }
        else if (s == gui.getInt("cancel.slot")) GuiManager.openCategory(p, ctx.cat);
        else {
            if (s == gui.getInt("add1.slot")) ctx.amount = Math.min(64, ctx.amount + 1);
            else if (s == gui.getInt("add10.slot")) ctx.amount = Math.min(64, ctx.amount + 10);
            else if (s == gui.getInt("set64.slot")) ctx.amount = 64;
            else if (s == gui.getInt("remove1.slot")) ctx.amount = Math.max(1, ctx.amount - 1);
            else if (s == gui.getInt("remove10.slot")) ctx.amount = Math.max(1, ctx.amount - 10);
            else if (s == gui.getInt("remove64.slot")) ctx.amount = 1;
            GuiManager.refreshConfirm(inv, ctx);
        }
    }

    private void buy(Player p, ShopContext ctx) {
        ArisShop m = ArisShop.getInstance();
        double t = ctx.price * ctx.amount;
        if (ctx.curr.equalsIgnoreCase("SHARDS")) {
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
    }
              }
