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
    private static final Map<UUID, ShopContext> session = new HashMap<>();

    public static class ShopContext {
        String category, itemId, currency, displayName;
        int amount = 1;
        double price;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        ArisShop main = ArisShop.getInstance();
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        String title = e.getView().getTitle();
        e.setCancelled(true);

        if (title.equals(main.color(main.getConfig().getString("main-menu.title")))) {
            ConfigurationSection sec = main.getConfig().getConfigurationSection("main-menu.categories");
            for (String key : sec.getKeys(false)) {
                if (e.getSlot() == sec.getInt(key + ".slot")) { GuiManager.openCategory(p, key); return; }
            }
        } else if (title.equals(main.color(main.getConfig().getString("gui.quantity-selector.title")))) {
            handleConfirm(p, e.getSlot(), e.getInventory());
        } else {
            if (e.getSlot() == main.getConfig().getInt("gui.back-button.slot")) { GuiManager.openMainMenu(p); return; }
            handleShop(p, title, e.getSlot());
        }
    }

    private void handleShop(Player p, String title, int slot) {
        ArisShop main = ArisShop.getInstance();
        File dir = new File(main.getDataFolder(), "shop");
        if (dir.listFiles() == null) return;
        for (File f : dir.listFiles()) {
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
            if (title.equals(main.color(conf.getString("title")))) {
                ConfigurationSection items = conf.getConfigurationSection("items");
                for (String key : items.getKeys(false)) {
                    if (slot == items.getInt(key + ".slot")) {
                        ShopContext ctx = new ShopContext();
                        ctx.category = f.getName().replace(".yml", "");
                        ctx.itemId = key;
                        ctx.price = items.getDouble(key + ".price");
                        ctx.displayName = main.color(items.getString(key + ".displayname"));
                        ctx.currency = conf.getString("currency", "VAULT");
                        session.put(p.getUniqueId(), ctx);
                        GuiManager.openConfirm(p, ctx);
                        return;
                    }
                }
            }
        }
    }

    private void handleConfirm(Player p, int slot, org.bukkit.inventory.Inventory inv) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection gui = main.getConfig().getConfigurationSection("gui.quantity-selector");
        ShopContext ctx = session.get(p.getUniqueId());
        if (ctx == null) return;

        if (slot == gui.getInt("add1.slot")) ctx.amount = Math.min(64, ctx.amount + 1);
        else if (slot == gui.getInt("add10.slot")) ctx.amount = Math.min(64, ctx.amount + 10);
        else if (slot == gui.getInt("set64.slot")) ctx.amount = 64;
        else if (slot == gui.getInt("remove1.slot")) ctx.amount = Math.max(1, ctx.amount - 1);
        else if (slot == gui.getInt("remove10.slot")) ctx.amount = Math.max(1, ctx.amount - 10);
        else if (slot == gui.getInt("remove64.slot")) ctx.amount = 1;
        else if (slot == gui.getInt("confirm.slot")) { executePurchase(p, ctx); p.closeInventory(); return; }
        else if (slot == gui.getInt("cancel.slot")) { GuiManager.openCategory(p, ctx.category); return; }
        
        GuiManager.refreshConfirm(inv, ctx);
        main.playSound(p, "button-click");
    }

    private void executePurchase(Player p, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        double total = ctx.price * ctx.amount;

        if (p.getInventory().firstEmpty() == -1) {
            p.sendMessage(main.color(main.getConfig().getString("messages.prefix") + "&cTúi đồ của bạn đã đầy!"));
            main.playSound(p, "purchase-fail");
            return;
        }

        if (ctx.currency.equalsIgnoreCase("SHARDS")) {
            String balStr = PlaceholderAPI.setPlaceholders(p, main.getConfig().getString("currencies.shards.balance-placeholder"));
            if (Double.parseDouble(balStr) < total) {
                p.sendMessage(main.color(main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.insufficient-shards.text")));
                main.playSound(p, "purchase-fail");
                return;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), main.getConfig().getString("currencies.shards.take-command").replace("%player%", p.getName()).replace("%price%", String.valueOf((int)total)));
        } else {
            if (ArisShop.getEconomy().getBalance(p) < total) {
                p.sendMessage(main.color(main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.insufficient-funds.text")));
                main.playSound(p, "purchase-fail");
                return;
            }
            ArisShop.getEconomy().withdrawPlayer(p, total);
        }

        File f = new File(main.getDataFolder() + "/shop", ctx.category + ".yml");
        YamlConfiguration cat = YamlConfiguration.loadConfiguration(f);
        Material mat = Material.valueOf(cat.getString("items." + ctx.itemId + ".material"));
        p.getInventory().addItem(new ItemStack(mat, ctx.amount));

        String msg = main.getConfig().getString("messages.buy-success.text").replace("%item%", ctx.displayName).replace("%amount%", String.valueOf(ctx.amount));
        p.sendMessage(main.color(main.getConfig().getString("messages.prefix") + msg));
        main.playSound(p, "purchase-success");
    }
          }
