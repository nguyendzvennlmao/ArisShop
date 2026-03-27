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

public class ShopItem {
    public static void open(Player p, String id) {
        ArisShop m = ArisShop.getInstance();
        File f = new File(m.getDataFolder() + "/shop", id + ".yml");
        if (!f.exists()) return;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        Inventory inv = Bukkit.createInventory(null, c.getInt("rows") * 9, m.color(c.getString("title")));
        ConfigurationSection items = c.getConfigurationSection("items");
        if (items != null) {
            for (String k : items.getKeys(false)) {
                inv.setItem(items.getInt(k + ".slot"), create(Material.valueOf(items.getString(k + ".material")), m.color(items.getString(k + ".displayname")), items.getStringList(k + ".lore"), items.getDouble(k + ".price")));
            }
        }
        p.openInventory(inv);
    }

    private static ItemStack create(Material m, String n, List<String> l, double p) {
        ItemStack i = new ItemStack(m);
        ItemMeta mt = i.getItemMeta();
        mt.setDisplayName(n);
        List<String> cl = new ArrayList<>();
        for (String s : l) cl.add(ArisShop.getInstance().color(s.replace("%price%", String.valueOf(p))));
        mt.setLore(cl);
        i.setItemMeta(mt);
        return i;
    }
}
