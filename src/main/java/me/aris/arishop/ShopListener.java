package me.aris.arishop;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class ShopListener implements Listener {
    private static final Map<UUID, ShopContext> session = new HashMap<>();

    private static class ShopContext {
        String category, itemId, currency;
        int amount;
        double price;
    }

    public static void openMainMenu(Player p) {
        ArisShop main = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(null, main.getConfig().getInt("main-menu.rows") * 9, main.color(main.getConfig().getString("main-menu.title")));
        ConfigurationSection sec = main.getConfig().getConfigurationSection("main-menu.categories");
        for (String key : sec.getKeys(false)) {
            inv.setItem(sec.getInt(key + ".slot"), createIcon(Material.valueOf(sec.getString(key + ".material")), main.color(sec.getString(key + ".displayname")), sec.getStringList(key + ".lore"), 0, 0));
        }
        p.openInventory(inv);
    }

    private void openCategory(Player p, String catId) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection cat = main.getConfig().getConfigurationSection("categories." + catId);
        Inventory inv = Bukkit.createInventory(null, cat.getInt("rows") * 9, main.color(cat.getString("title")));
        ConfigurationSection items = cat.getConfigurationSection("items");
        for (String key : items.getKeys(false)) {
            inv.setItem(items.getInt(key + ".slot"), createIcon(Material.valueOf(items.getString(key + ".material")), main.color(items.getString(key + ".displayname")), items.getStringList(key + ".lore"), items.getDouble(key + ".price"), 1));
        }
        p.openInventory(inv);
    }

    private void openConfirmGUI(Player p, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(null, main.getConfig().getInt("gui.quantity-selector.rows") * 9, main.color(main.getConfig().getString("gui.quantity-selector.title")));
        refreshConfirmGUI(inv, ctx);
        p.openInventory(inv);
    }

    private void refreshConfirmGUI(Inventory inv, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection gui = main.getConfig().getConfigurationSection("gui.quantity-selector");
        inv.setItem(gui.getInt("confirm-button.slot"), createIcon(Material.valueOf(gui.getString("confirm-button.material")), main.color(gui.getString("confirm-button.displayname")), gui.getStringList("confirm-button.lore"), ctx.price * ctx.amount, ctx.amount));
        inv.setItem(gui.getInt("cancel-button.slot"), createIcon(Material.valueOf(gui.getString("cancel-button.material")), main.color(gui.getString("cancel-button.displayname")), gui.getStringList("cancel-button.lore"), 0, 0));
        Material m = Material.valueOf(main.getConfig().getString("categories." + ctx.category + ".items." + ctx.itemId + ".material"));
        ItemStack preview = createIcon(m, main.color(main.getConfig().getString("categories." + ctx.category + ".items." + ctx.itemId + ".displayname")), gui.getStringList("item-preview.lore"), ctx.price, ctx.amount);
        preview.setAmount(ctx.amount);
        inv.setItem(gui.getInt("item-preview.slot"), preview);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        ArisShop main = ArisShop.getInstance();
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        String title = e.getView().getTitle();

        if (title.equals(main.color(main.getConfig().getString("main-menu.title")))) {
            e.setCancelled(true);
            ConfigurationSection sec = main.getConfig().getConfigurationSection("main-menu.categories");
            for (String key : sec.getKeys(false)) {
                if (e.getSlot() == sec.getInt(key + ".slot")) { openCategory(p, key); return; }
            }
        } else if (title.contains(main.color("ѕʜᴏᴘ -"))) {
            e.setCancelled(true);
            handleCategoryClick(p, title, e.getSlot());
        } else if (title.equals(main.color(main.getConfig().getString("gui.quantity-selector.title")))) {
            e.setCancelled(true);
            handleConfirmClick(p, e.getSlot(), e.getClick(), e.getInventory());
        }
    }

    private void handleCategoryClick(Player p, String title, int slot) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection cats = main.getConfig().getConfigurationSection("categories");
        for (String catKey : cats.getKeys(false)) {
            if (title.equals(main.color(cats.getString(catKey + ".title")))) {
                ConfigurationSection items = cats.getConfigurationSection(catKey + ".items");
                for (String itemKey : items.getKeys(false)) {
                    if (slot == items.getInt(itemKey + ".slot")) {
                        ShopContext ctx = new ShopContext();
                        ctx.category = catKey; ctx.itemId = itemKey; ctx.amount = 1;
                        ctx.price = items.getDouble(itemKey + ".price");
                        ctx.currency = cats.getString(catKey + ".currency", "VAULT");
                        session.put(p.getUniqueId(), ctx);
                        openConfirmGUI(p, ctx);
                        return;
                    }
                }
            }
        }
    }

    private void handleConfirmClick(Player p, int slot, ClickType click, Inventory inv) {
        ShopContext ctx = session.get(p.getUniqueId());
        if (ctx == null) return;
        if (slot == 13) {
            if (click == ClickType.LEFT) ctx.amount = Math.min(64, ctx.amount + 1);
            else if (click == ClickType.RIGHT) ctx.amount = Math.max(1, ctx.amount - 1);
            else if (click == ClickType.SHIFT_LEFT) ctx.amount = Math.min(64, ctx.amount + 10);
            else if (click == ClickType.SHIFT_RIGHT) ctx.amount = Math.max(1, ctx.amount - 10);
            refreshConfirmGUI(inv, ctx);
        } else if (slot == 15) {
            executePurchase(p, ctx);
            p.closeInventory();
        } else if (slot == 11) p.closeInventory();
    }

    private void executePurchase(Player p, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        double total = ctx.price * ctx.amount;
        if (ctx.currency.equalsIgnoreCase("SHARDS")) {
            String bal = PlaceholderAPI.setPlaceholders(p, main.getConfig().getString("currencies.shards.balance-placeholder"));
            if (Double.parseDouble(bal) < total) {
                p.sendMessage(main.color(main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.insufficient-shards")));
                return;
            }
            String cmd = main.getConfig().getString("currencies.shards.take-command").replace("%player%", p.getName()).replace("%price%", String.valueOf((int)total));
            Bukkit.getGlobalRegionScheduler().execute(main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            p.sendMessage(main.color(main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.item-bought-shards").replace("%amount%", String.valueOf(ctx.amount)).replace("%item%", ctx.itemId).replace("%price%", String.valueOf((int)total))));
        } else {
            if (ArisShop.getEconomy().getBalance(p) < total) {
                p.sendMessage(main.color(main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.insufficient-funds")));
                return;
            }
            ArisShop.getEconomy().withdrawPlayer(p, total);
            p.sendMessage(main.color(main.getConfig().getString("messages.prefix") + main.getConfig().getString("messages.item-bought").replace("%amount%", String.valueOf(ctx.amount)).replace("%item%", ctx.itemId).replace("%price%", String.valueOf(total))));
        }
        String give = main.getConfig().getString("categories." + ctx.category + ".items." + ctx.itemId + ".command", "give %player% " + main.getConfig().getString("categories." + ctx.category + ".items." + ctx.itemId + ".material") + " %amount%");
        Bukkit.getGlobalRegionScheduler().execute(main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), give.replace("%player%", p.getName()).replace("%amount%", String.valueOf(ctx.amount))));
    }

    private static ItemStack createIcon(Material m, String name, List<String> lore, double price, int amount) {
        ArisShop main = ArisShop.getInstance();
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> list = new ArrayList<>();
        for (String s : lore) list.add(main.color(s.replace("%price%", String.valueOf(price)).replace("%total_price%", String.valueOf(price)).replace("%amount%", String.valueOf(amount))));
        meta.setLore(list);
        item.setItemMeta(meta);
        return item;
    }
}
