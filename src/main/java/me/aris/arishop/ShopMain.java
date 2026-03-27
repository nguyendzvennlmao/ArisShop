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
        try {
            Inventory inv = Bukkit.createInventory(null, m.getConfig().getInt("main-menu.rows") * 9, m.color(m.getConfig().getString("main-menu.title")));
            ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    Material mat = Material.matchMaterial(sec.getString(key + ".material").toUpperCase());
                    if (mat == null) mat = Material.BARRIER;
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(m.color(sec.getString(key + ".displayname")));
                        List<String> lore = new ArrayList<>();
                        for (String s : sec.getStringList(key + ".lore")) lore.add(m.color(s));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                    inv.setItem(sec.getInt(key + ".slot"), item);
                }
            }
            p.openInventory(inv);
        } catch (Exception e) {
            p.sendMessage("§c[ArisShop] Lỗi Main Menu! Kiểm tra config.yml");
        }
    }
                                           }
