package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfirmPurchase {
    public static void open(Player p, ShopListener.ShopContext ctx) {
        ArisShop m = ArisShop.getInstance();
        String title = m.color(m.getConfig().getString("gui.quantity-selector.title"));
        Inventory inv = Bukkit.createInventory(null, m.getConfig().getInt("gui.quantity-selector.rows") * 9, title);
        update(inv, ctx);
        p.openInventory(inv);
    }

    public static void update(Inventory inv, ShopListener.ShopContext ctx) {
        ArisShop m = ArisShop.getInstance();
        ConfigurationSection gui = m.getConfig().getConfigurationSection("gui.quantity-selector");
        inv.clear();
        String[] buttons = {"cancel", "confirm", "add1", "add10", "set64", "remove1", "remove10", "remove64"};
        for (String s : buttons) {
            ConfigurationSection sec = gui.getConfigurationSection(s);
            inv.setItem(sec.getInt("slot"), createItem(Material.valueOf(sec.getString("material")), m.color(sec.getString("name").replace("%amount%", String.valueOf(ctx.amount))), sec.getStringList("lore"), ctx.price * ctx.amount));
        }
        File f = new File(m.getDataFolder() + "/shop", ctx.cat + ".yml");
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        inv.setItem(gui.getInt("item-preview.slot"), createItem(Material.valueOf(c.getString("items." + ctx.item + ".material")), ctx.name, gui.getStringList("item-preview.lore"), ctx.price * ctx.amount));
    }

    private static ItemStack createItem(Material m, String n, List<String> l, double tp) {
        ItemStack i = new ItemStack(m);
        ItemMeta mt = i.getItemMeta();
        mt.setDisplayName(n);
        List<String> cl = new ArrayList<>();
        for (String s : l) cl.add(ArisShop.getInstance().color(s.replace("%total_price%", String.valueOf(tp))));
        mt.setLore(cl);
        i.setItemMeta(mt);
        return i;
    }
}
