package me.aris.arishop;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.*;

public class ShopListener implements Listener {
    private static final Map<UUID, ShopContext> session = new HashMap<>();

    private static class ShopContext {
        String category, itemId, currency, displayName;
        int amount;
        double price;
    }

    public static void openMainMenu(Player p) {
        ArisShop main = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(null, main.getConfig().getInt("main-menu.rows") * 9, main.color(main.getConfig().getString("main-menu.title")));
        ConfigurationSection sec = main.getConfig().getConfigurationSection("main-menu.categories");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                inv.setItem(sec.getInt(key + ".slot"), createIcon(Material.valueOf(sec.getString(key + ".material")), main.color(sec.getString(key + ".displayname")), sec.getStringList(key + ".lore"), 0, 0));
            }
        }
        main.playSound(p, "menu-open");
        p.openInventory(inv);
    }

    private void openCategory(Player p, String catId) {
        ArisShop main = ArisShop.getInstance();
        File file = new File(main.getDataFolder() + "/shop", catId + ".yml");
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Inventory inv = Bukkit.createInventory(null, config.getInt("rows") * 9, main.color(config.getString("title")));
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                inv.setItem(items.getInt(key + ".slot"), createIcon(Material.valueOf(items.getString(key + ".material")), main.color(items.getString(key + ".displayname")), items.getStringList(key + ".lore"), items.getDouble(key + ".price"), 1));
            }
        }
        main.playSound(p, "menu-open");
        p.openInventory(inv);
    }

    private void refreshConfirmGUI(Inventory inv, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection gui = main.getConfig().getConfigurationSection("gui.quantity-selector");
        String[] actions = {"cancel", "confirm", "add1", "add10", "set64", "remove1", "remove10", "remove64"};
        for (String action : actions) {
            ConfigurationSection btn = gui.getConfigurationSection(action);
            int amt = btn.getInt("stack-change", 1);
            inv.setItem(btn.getInt("slot"), createIcon(Material.valueOf(btn.getString("material")), 
                main.color(btn.getString("name").replace("%amount%", String.valueOf(Math.abs(amt)))), 
                btn.getStringList("lore"), ctx.price * ctx.amount, ctx.amount));
        }
        File f = new File(main.getDataFolder() + "/shop", ctx.category + ".yml");
        YamlConfiguration catConfig = YamlConfiguration.loadConfiguration(f);
        Material m = Material.valueOf(catConfig.getString("items." + ctx.itemId + ".material"));
        ItemStack preview = createIcon(m, main.color(catConfig.getString("items." + ctx.itemId + ".displayname")), gui.getStringList("item-preview.lore"), ctx.price, ctx.amount);
        preview.setAmount(ctx.amount);
        inv.setItem(gui.getInt("item-preview.slot"), preview);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        ArisShop main = ArisShop.getInstance();
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        String title = e.getView().getTitle();
        e.setCancelled(true);

        if (title.equals(main.color(main.getConfig().getString("main-menu.title")))) {
            ConfigurationSection sec = main.getConfig().getConfigurationSection("main-menu.categories");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    if (e.getSlot() == sec.getInt(key + ".slot")) { openCategory(p, key); return; }
                }
            }
        } else if (title.equals(main.color(main.getConfig().getString("gui.quantity-selector.title")))) {
            handleConfirmClick(p, e.getSlot(), e.getInventory());
        } else {
            handleCategoryClick(p, title, e.getSlot());
        }
    }

    private void handleConfirmClick(Player p, int slot, Inventory inv) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection gui = main.getConfig().getConfigurationSection("gui.quantity-selector");
        ShopContext ctx = session.get(p.getUniqueId());
        if (ctx == null) return;

        if (slot == gui.getInt("add1.slot") || slot == gui.getInt("add10.slot") || slot == gui.getInt("set64.slot") || 
            slot == gui.getInt("remove1.slot") || slot == gui.getInt("remove10.slot") || slot == gui.getInt("remove64.slot")) {
            
            if (slot == gui.getInt("add1.slot")) ctx.amount = Math.min(64, ctx.amount + 1);
            else if (slot == gui.getInt("add10.slot")) ctx.amount = Math.min(64, ctx.amount + 10);
            else if (slot == gui.getInt("set64.slot")) ctx.amount = 64;
            else if (slot == gui.getInt("remove1.slot")) ctx.amount = Math.max(1, ctx.amount - 1);
            else if (slot == gui.getInt("remove10.slot")) ctx.amount = Math.max(1, ctx.amount - 10);
            else if (slot == gui.getInt("remove64.slot")) ctx.amount = 1;
            
            main.playSound(p, "button-click");
            refreshConfirmGUI(inv, ctx);
        } else if (slot == gui.getInt("confirm.slot")) {
            executePurchase(p, ctx);
            p.closeInventory();
        } else if (slot == gui.getInt("cancel.slot")) {
            main.playSound(p, "cancel-click");
            openCategory(p, ctx.category);
        }
    }

    private void executePurchase(Player p, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        File f = new File(main.getDataFolder() + "/shop", ctx.category + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        double total = ctx.price * ctx.amount;

        if (p.getInventory().firstEmpty() == -1) {
            handleMessage(p, "full-inventory", null, 0, p);
            main.playSound(p, "purchase-fail");
            return;
        }

        if (ctx.currency.equalsIgnoreCase("SHARDS")) {
            String balStr = PlaceholderAPI.setPlaceholders(p, main.getConfig().getString("currencies.shards.balance-placeholder"));
            double bal = 0;
            try { bal = Double.parseDouble(balStr); } catch (Exception e) { bal = 0; }
            if (bal < total) {
                main.playSound(p, "purchase-fail");
                handleMessage(p, "insufficient-shards", null, 0, p);
                return;
            }
            String cmd = main.getConfig().getString("currencies.shards.take-command").replace("%player%", p.getName()).replace("%price%", String.valueOf((int)total));
            Bukkit.getGlobalRegionScheduler().execute(main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        } else {
            if (ArisShop.getEconomy().getBalance(p) < total) {
                main.playSound(p, "purchase-fail");
                handleMessage(p, "insufficient-funds", null, 0, p);
                return;
            }
            ArisShop.getEconomy().withdrawPlayer(p, total);
        }

        main.playSound(p, "purchase-success");
        String give = config.getString("items." + ctx.itemId + ".command").replace("%player%", p.getName()).replace("%amount%", String.valueOf(ctx.amount));
        Bukkit.getGlobalRegionScheduler().execute(main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), give));
        handleMessage(p, "buy-success", ctx.displayName, ctx.amount, p);
    }

    public static void handleMessage(Player p, String key, String item, int amount, CommandSender sender) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection msgSec = main.getConfig().getConfigurationSection("messages." + key);
        if (msgSec == null) return;

        String rawMsg = msgSec.getString("text");
        String formatted = main.color(main.getConfig().getString("messages.prefix") + rawMsg.replace("%item%", item != null ? item : "").replace("%amount%", String.valueOf(amount)));
        
        if (p != null) {
            if (msgSec.getBoolean("chat")) p.sendMessage(formatted);
            if (msgSec.getBoolean("actionbar")) p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(formatted));
        } else {
            sender.sendMessage(formatted);
        }
    }

    private void handleCategoryClick(Player p, String title, int slot) {
        ArisShop main = ArisShop.getInstance();
        File folder = new File(main.getDataFolder(), "shop");
        if (!folder.exists() || folder.listFiles() == null) return;
        for (File f : folder.listFiles()) {
            if (!f.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
            if (title.equals(main.color(config.getString("title")))) {
                ConfigurationSection items = config.getConfigurationSection("items");
                if (items == null) continue;
                for (String key : items.getKeys(false)) {
                    if (slot == items.getInt(key + ".slot")) {
                        ShopContext ctx = new ShopContext();
                        ctx.category = f.getName().replace(".yml", "");
                        ctx.itemId = key; ctx.amount = 1;
                        ctx.price = items.getDouble(key + ".price");
                        ctx.displayName = config.getString("items." + key + ".displayname");
                        ctx.currency = config.getString("currency", "VAULT");
                        session.put(p.getUniqueId(), ctx);
                        Inventory confirmInv = Bukkit.createInventory(null, main.getConfig().getInt("gui.quantity-selector.rows") * 9, main.color(main.getConfig().getString("gui.quantity-selector.title")));
                        refreshConfirmGUI(confirmInv, ctx);
                        main.playSound(p, "menu-open");
                        p.openInventory(confirmInv);
                        return;
                    }
                }
            }
        }
    }

    private static ItemStack createIcon(Material m, String name, List<String> lore, double total_price, int amount) {
        ArisShop main = ArisShop.getInstance();
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (name != null) meta.setDisplayName(name);
        if (lore != null) {
            List<String> list = new ArrayList<>();
            for (String s : lore) list.add(main.color(s.replace("%total_price%", String.valueOf(total_price)).replace("%price%", String.valueOf(total_price/Math.max(1, amount))).replace("%amount%", String.valueOf(amount))));
            meta.setLore(list);
        }
        item.setItemMeta(meta);
        return item;
    }
    }
