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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {
    private static final Map<UUID, ShopContext> pendingBuy = new HashMap<>();

    private static class ShopContext {
        String category;
        String itemId;
        int amount;
        double price;
        String currency;
    }

    public static void openMainMenu(Player p) {
        ArisShop main = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(null, main.getConfig().getInt("main-menu.rows") * 9, main.color(main.getConfig().getString("main-menu.title")));
        ConfigurationSection sec = main.getConfig().getConfigurationSection("main-menu.categories");
        for (String key : sec.getKeys(false)) {
            inv.setItem(sec.getInt(key + ".slot"), createItem(Material.valueOf(sec.getString(key + ".material")), main.color(sec.getString(key + ".displayname")), sec.getStringList(key + ".lore")));
        }
        p.openInventory(inv);
    }

    private static void openCategory(Player p, String catId) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection cat = main.getConfig().getConfigurationSection("categories." + catId);
        Inventory inv = Bukkit.createInventory(null, cat.getInt("rows") * 9, main.color(cat.getString("title")));
        ConfigurationSection items = cat.getConfigurationSection("items");
        for (String key : items.getKeys(false)) {
            inv.setItem(items.getInt(key + ".slot"), createItem(Material.valueOf(items.getString(key + ".material")), main.color(items.getString(key + ".displayname")), items.getStringList(key + ".lore"), items.getDouble(key + ".price")));
        }
        p.openInventory(inv);
    }

    private static void openQuantityGUI(Player p, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(null, main.getConfig().getInt("gui.quantity-selector.rows") * 9, main.color(main.getConfig().getString("gui.quantity-selector.title")));
        updateQuantityGUI(inv, ctx);
        p.openInventory(inv);
    }

    private static void updateQuantityGUI(Inventory inv, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection gui = main.getConfig().getConfigurationSection("gui.quantity-selector");
        
        ItemStack confirm = createItem(Material.valueOf(gui.getString("confirm-button.material")), main.color(gui.getString("confirm-button.displayname")), gui.getStringList("confirm-button.lore"), ctx.price * ctx.amount, ctx.amount);
        inv.setItem(gui.getInt("confirm-button.slot"), confirm);

        ItemStack cancel = createItem(Material.valueOf(gui.getString("cancel-button.material")), main.color(gui.getString("cancel-button.displayname")), gui.getStringList("cancel-button.lore"));
        inv.setItem(gui.getInt("cancel-button.slot"), cancel);

        Material previewMat = Material.valueOf(main.getConfig().getString("categories." + ctx.category + ".items." + ctx.itemId + ".material"));
        ItemStack preview = createItem(previewMat, main.color(main.getConfig().getString("categories." + ctx.category + ".items." + ctx.itemId + ".displayname")), gui.getStringList("item-preview.lore"), ctx.price, ctx.amount);
        preview.setAmount(ctx.amount);
        inv.setItem(gui.getInt("item-preview.slot"), preview);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        ArisShop main = ArisShop.getInstance();
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        if (e.getCurrentItem() == null) return;

        if (title.equals(main.color(main.getConfig().getString("main-menu.title")))) {
            e.setCancelled(true);
            ConfigurationSection sec = main.getConfig().getConfigurationSection("main-menu.categories");
            for (String key : sec.getKeys(false)) {
                if (e.getSlot() == sec.getInt(key + ".slot")) {
                    openCategory(p, key);
                    return;
                }
            }
        } else if (title.contains(main.color("ѕʜᴏᴘ -"))) {
            e.setCancelled(true);
            String catId = title.split("- ")[1].toLowerCase();
            ConfigurationSection items = main.getConfig().getConfigurationSection("categories." + catId + ".items");
            for (String key : items.getKeys(false)) {
                if (e.getSlot() == items.getInt(key + ".slot")) {
                    ShopContext ctx = new ShopContext();
                    ctx.category = catId;
                    ctx.itemId = key;
                    ctx.amount = 1;
                    ctx.price = items.getDouble(key + ".price");
                    ctx.currency = main.getConfig().getString("categories." + catId + ".currency", "VAULT");
                    pendingBuy.put(p.getUniqueId(), ctx);
                    openQuantityGUI(p, ctx);
                    return;
                }
            }
        } else if (title.equals(main.color(main.getConfig().getString("gui.quantity-selector.title")))) {
            e.setCancelled(true);
            ShopContext ctx = pendingBuy.get(p.getUniqueId());
            if (ctx == null) return;
            int slot = e.getSlot();
            if (slot == 13) {
                ClickType click = e.getClick();
                if (click == ClickType.LEFT) ctx.amount = Math.min(64, ctx.amount + 1);
                else if (click == ClickType.RIGHT) ctx.amount = Math.max(1, ctx.amount - 1);
                else if (click == ClickType.SHIFT_LEFT) ctx.amount = Math.min(64, ctx.amount + 10);
                else if (click == ClickType.SHIFT_RIGHT) ctx.amount = Math.max(1, ctx.amount - 10);
                updateQuantityGUI(e.getInventory(), ctx);
            } else if (slot == 15) {
                processPurchase(p, ctx);
                p.closeInventory();
            } else if (slot == 11) {
                p.closeInventory();
            }
        }
    }

    private void processPurchase(Player p, ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        double total = ctx.price * ctx.amount;
        if (ctx.currency.equalsIgnoreCase("SHARDS")) {
            String balStr = PlaceholderAPI.setPlaceholders(p, main.getConfig().getString("currencies.shards.balance-placeholder"));
            if (Double.parseDouble(balStr) < total) {
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
        String giveCmd = main.getConfig().getString("categories." + ctx.category + ".items." + ctx.itemId + ".command", "give %player% " + main.getConfig().getString("categories." + ctx.category + ".items." + ctx.itemId + ".material") + " %amount%");
        Bukkit.getGlobalRegionScheduler().execute(main, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), giveCmd.replace("%player%", p.getName()).replace("%amount%", String.valueOf(ctx.amount))));
    }

    private static ItemStack createItem(Material m, String name, List<String> lore) { return createItem(m, name, lore, 0, 0); }
    private static ItemStack createItem(Material m, String name, List<String> lore, double price, int amount) {
        ArisShop main = ArisShop.getInstance();
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> coloredLore = new ArrayList<>();
        for (String s : lore) coloredLore.add(main.color(s.replace("%price%", String.valueOf(price)).replace("%total_price%", String.valueOf(price)).replace("%amount%", String.valueOf(amount))));
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        return item;
    }
                                                              }
