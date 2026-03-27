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
    public static class MainMenuHolder implements InventoryHolder { @Override public Inventory getInventory() { return null; } }
    public static class CategoryHolder implements InventoryHolder { 
        public String id; 
        public CategoryHolder(String id) { this.id = id; }
        @Override public Inventory getInventory() { return null; } 
    }
    public static class ConfirmHolder implements InventoryHolder { @Override public Inventory getInventory() { return null; } }

    public static void openMainMenu(Player p) {
        ArisShop main = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(new MainMenuHolder(), main.getConfig().getInt("main-menu.rows") * 9, main.color(main.getConfig().getString("main-menu.title")));
        ConfigurationSection sec = main.getConfig().getConfigurationSection("main-menu.categories");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                inv.setItem(sec.getInt(key + ".slot"), createItem(Material.valueOf(sec.getString(key + ".material")), main.color(sec.getString(key + ".displayname")), sec.getStringList(key + ".lore"), 0, 1));
            }
        }
        main.playSound(p, "menu-open");
        p.openInventory(inv);
    }

    public static void openCategory(Player p, String catId) {
        ArisShop main = ArisShop.getInstance();
        File f = new File(main.getDataFolder() + "/shop", catId + ".yml");
        if (!f.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        Inventory inv = Bukkit.createInventory(new CategoryHolder(catId), config.getInt("rows") * 9, main.color(config.getString("title")));
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                inv.setItem(items.getInt(key + ".slot"), createItem(Material.valueOf(items.getString(key + ".material")), main.color(items.getString(key + ".displayname")), items.getStringList(key + ".lore"), items.getDouble(key + ".price"), 1));
            }
        }
        ConfigurationSection back = main.getConfig().getConfigurationSection("gui.back-button");
        if (back != null) inv.setItem(back.getInt("slot"), createItem(Material.valueOf(back.getString("material")), main.color(back.getString("displayname")), back.getStringList("lore"), 0, 1));
        p.openInventory(inv);
    }

    public static void openConfirm(Player p, ShopListener.ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(new ConfirmHolder(), main.getConfig().getInt("gui.quantity-selector.rows") * 9, main.color(main.getConfig().getString("gui.quantity-selector.title")));
        refreshConfirm(inv, ctx);
        p.openInventory(inv);
    }

    public static void refreshConfirm(Inventory inv, ShopListener.ShopContext ctx) {
        ArisShop main = ArisShop.getInstance();
        ConfigurationSection gui = main.getConfig().getConfigurationSection("gui.quantity-selector");
        inv.clear();
        String[] actions = {"cancel", "confirm", "add1", "add10", "set64", "remove1", "remove10", "remove64"};
        for (String s : actions) {
            ConfigurationSection b = gui.getConfigurationSection(s);
            if (b != null) inv.setItem(b.getInt("slot"), createItem(Material.valueOf(b.getString("material")), main.color(b.getString("name").replace("%amount%", String.valueOf(ctx.amount))), b.getStringList("lore"), ctx.price * ctx.amount, ctx.amount));
        }
        File f = new File(main.getDataFolder() + "/shop", ctx.category + ".yml");
        YamlConfiguration cat = YamlConfiguration.loadConfiguration(f);
        ItemStack preview = createItem(Material.valueOf(cat.getString("items." + ctx.itemId + ".material")), ctx.displayName, gui.getStringList("item-preview.lore"), ctx.price * ctx.amount, ctx.amount);
        preview.setAmount(ctx.amount);
        inv.setItem(gui.getInt("item-preview.slot"), preview);
    }

    private static ItemStack createItem(Material m, String n, List<String> l, double tp, int a) {
        ArisShop main = ArisShop.getInstance();
        ItemStack i = new ItemStack(m);
        ItemMeta mt = i.getItemMeta();
        if (mt != null) {
            mt.setDisplayName(n);
            if (l != null) {
                List<String> nl = new ArrayList<>();
                for (String s : l) nl.add(main.color(s.replace("%total_price%", String.valueOf(tp)).replace("%price%", String.valueOf(tp/a)).replace("%amount%", String.valueOf(a))));
                mt.setLore(nl);
            }
            i.setItemMeta(mt);
        }
        return i;
    }
  }
