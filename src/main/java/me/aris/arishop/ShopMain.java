package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ShopMain {
    public static void open(Player p) {
        ArisShop m = ArisShop.getInstance();
        String title = m.color(m.getConfig().getString("main-menu.title"));
        Inventory inv = Bukkit.createInventory(null, m.getConfig().getInt("main-menu.rows") * 9, title);
        ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                inv.setItem(sec.getInt(key + ".slot"), create(Material.valueOf(sec.getString(key + ".material")), m.color(sec.getString(key + ".displayname")), sec.getStringList(key + ".lore")));
            }
        }
        m.playSound(p, "menu-open");
        p.openInventory(inv);
    }

    public static ItemStack create(Material m, String n, List<String> l) {
        ItemStack i = new ItemStack(m);
        ItemMeta mt = i.getItemMeta();
        mt.setDisplayName(n);
        List<String> cl = new ArrayList<>();
        for (String s : l) cl.add(ArisShop.getInstance().color(s));
        mt.setLore(cl);
        i.setItemMeta(mt);
        return i;
    }
    }
