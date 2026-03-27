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
    public static void open(Player p, String inputName) {
        ArisShop m = ArisShop.getInstance();
        File f = new File(m.getDataFolder() + "/shop", inputName.replace(".yml", "") + ".yml");
        if (!f.exists()) return;
        try {
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            ConfigurationSection d = c;
            if (c.getKeys(false).size() == 1 && !c.contains("items"))
                d = c.getConfigurationSection(c.getKeys(false).iterator().next());

            Inventory inv = Bukkit.createInventory(null, d.getInt("rows", 3) * 9, m.color(d.getString("title")));
            ConfigurationSection items = d.getConfigurationSection("items");
            if (items != null) {
                for (String k : items.getKeys(false)) {
                    Material mat = Material.matchMaterial(items.getString(k + ".material", "STONE"));
                    ItemStack item = new ItemStack(mat == null ? Material.BARRIER : mat);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(m.color(items.getString(k + ".displayname")));
                    List<String> lore = new ArrayList<>();
                    double price = items.getDouble(k + ".price");
                    for (String s : items.getStringList(k + ".lore")) lore.add(m.color(s.replace("%price%", String.valueOf(price))));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inv.setItem(items.getInt(k + ".slot"), item);
                }
            }
            p.openInventory(inv);
        } catch (Exception e) { p.sendMessage("§c[ArisShop] Lỗi file shop!"); }
    }
                    }
