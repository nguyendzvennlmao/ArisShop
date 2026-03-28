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
        ConfigurationSection menu = m.getConfig().getConfigurationSection("main-menu");
        if (menu == null) return;
        Inventory inv = Bukkit.createInventory(null, menu.getInt("rows", 3) * 9, m.color(menu.getString("title")));
        ConfigurationSection cats = menu.getConfigurationSection("categories");
        if (cats != null) {
            for (String k : cats.getKeys(false)) {
                ItemStack item = new ItemStack(Material.valueOf(cats.getString(k + ".material")));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(m.color(cats.getString(k + ".displayname")));
                    List<String> lore = new ArrayList<>();
                    for (String s : cats.getStringList(k + ".lore")) lore.add(m.color(s));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(cats.getInt(k + ".slot"), item);
            }
        }
        p.openInventory(inv);
    }
                                               }
