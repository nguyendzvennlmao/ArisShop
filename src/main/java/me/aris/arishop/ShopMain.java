package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.stream.Collectors;

public class ShopMain {
    public static void open(Player p) {
        ArisShop m = ArisShop.getInstance();
        try {
            int rows = m.getConfig().getInt("main-menu.rows", 3);
            Inventory inv = Bukkit.createInventory(null, rows * 9, m.color(m.getConfig().getString("main-menu.title")));
            ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    Material mat = Material.matchMaterial(sec.getString(key + ".material", "PAPER"));
                    ItemStack item = new ItemStack(mat == null ? Material.BARRIER : mat);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(m.color(sec.getString(key + ".displayname")));
                    meta.setLore(sec.getStringList(key + ".lore").stream().map(m::color).collect(Collectors.toList()));
                    item.setItemMeta(meta);
                    inv.setItem(sec.getInt(key + ".slot"), item);
                }
            }
            p.openInventory(inv);
            p.playSound(p.getLocation(), org.bukkit.Sound.valueOf(m.getConfig().getString("sounds.menu-open")), 1, 1);
        } catch (Exception e) { p.sendMessage("§c[ArisShop] Lỗi Main Menu!"); }
    }
                        }
