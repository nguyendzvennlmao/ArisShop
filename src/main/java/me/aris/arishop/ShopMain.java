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
                ItemStack item = new ItemStack(Material.valueOf(sec.getString(key + ".material")));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(m.color(sec.getString(key + ".displayname")));
                List<String> lore = new ArrayList<>();
                for (String s : sec.getStringList(key + ".lore")) lore.add(m.color(s));
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(sec.getInt(key + ".slot"), item);
            }
        }
        p.openInventory(inv);
    }
}
