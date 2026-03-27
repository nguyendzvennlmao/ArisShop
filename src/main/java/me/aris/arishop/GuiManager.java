package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GuiManager {
    public static class MenuHolder implements InventoryHolder {
        public String type, id;
        public MenuHolder(String type, String id) { this.type = type; this.id = id; }
        @Override public Inventory getInventory() { return null; }
    }

    public static void openMainMenu(Player p) {
        ArisShop m = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(new MenuHolder("MAIN", null), m.getConfig().getInt("main-menu.rows") * 9, m.color(m.getConfig().getString("main-menu.title")));
        ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                inv.setItem(sec.getInt(key + ".slot"), createItem(Material.valueOf(sec.getString(key + ".material")), m.color(sec.getString(key + ".displayname")), sec.getStringList(key + ".lore"), 0, 1));
            }
        }
        m.playSound(p, "menu-open");
        p.openInventory(inv);
    }

    public static void openCategory(Player p, String id) {
        ArisShop m = ArisShop.getInstance();
        File f = new File(m.getDataFolder() + "/shop", id + ".yml");
        if (!f.exists()) return;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        Inventory inv = Bukkit.createInventory(new MenuHolder("SHOP", id), c.getInt("rows") * 9, m.color(c.getString("title")));
        ConfigurationSection items = c.getConfigurationSection("items");
        if (items != null) {
            for (String k : items.getKeys(false)) {
                inv.setItem(items.getInt(k + ".slot"), createItem(Material.valueOf(items.getString(k + ".material")), m.color(items.getString(k + ".displayname")), items.getStringList(k + ".lore"), items.getDouble(k + ".price"), 1));
            }
        }
        p.openInventory(inv);
    }

    public static void openConfirm(Player p, ShopListener.ShopContext ctx) {
        ArisShop m = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(new MenuHolder("CONFIRM", null), m.getConfig().getInt("gui.quantity-selector.rows") * 9, m.color(m.getConfig().getString("gui.quantity-selector.title")));
        refreshConfirm(inv, ctx);
        p.openInventory(inv);
    }

    public static void refreshConfirm(Inventory inv, ShopListener.ShopContext ctx) {
        ArisShop m = ArisShop.getInstance();
        ConfigurationSection gui = m.getConfig().getConfigurationSection("gui.quantity-selector");
        inv.clear();
        String[] acts = {"cancel", "confirm", "add1", "add10", "set64", "remove1", "remove10", "remove64"};
        for (String s : acts) {
            ConfigurationSection b = gui.getConfigurationSection(s);
            if (b != null) inv.setItem(b.getInt("slot"), createItem(Material.valueOf(b.getString("material")), m.color(b.getString("name").replace("%amount%", String.valueOf(ctx.amount))), b.getStringList("lore"), ctx.price * ctx.amount, ctx.amount));
        }
        File f = new File(m.getDataFolder() + "/shop", ctx.cat + ".yml");
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        ItemStack pre = createItem(Material.valueOf(c.getString("items." + ctx.item + ".material")), ctx.name, gui.getStringList("item-preview.lore"), ctx.price * ctx.amount, ctx.amount);
        pre.setAmount(ctx.amount);
        inv.setItem(gui.getInt("item-preview.slot"), pre);
    }

    private static ItemStack createItem(Material m, String n, List<String> l, double tp, int a) {
        ArisShop ms = ArisShop.getInstance();
        ItemStack i = new ItemStack(m);
        ItemMeta mt = i.getItemMeta();
        if (mt != null) {
            mt.setDisplayName(n);
            if (l != null) {
                List<String> nl = new ArrayList<>();
                for (String s : l) nl.add(ms.color(s.replace("%total_price%", String.valueOf(tp)).replace("%price%", String.valueOf(tp/a)).replace("%amount%", String.valueOf(a))));
                mt.setLore(nl);
            }
            i.setItemMeta(mt);
        }
        return i;
    }
          }
