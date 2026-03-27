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
        String fileName = inputName.replace(".yml", "") + ".yml";
        File f = new File(m.getDataFolder() + "/shop", fileName);
        if (!f.exists()) {
            p.sendMessage("§c[ArisShop] Thiếu file: shop/" + fileName);
            return;
        }
        try {
            YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
            Inventory inv = Bukkit.createInventory(null, c.getInt("rows") * 9, m.color(c.getString("title")));
            ConfigurationSection items = c.getConfigurationSection("items");
            if (items != null) {
                for (String k : items.getKeys(false)) {
                    Material mat = Material.matchMaterial(items.getString(k + ".material").toUpperCase());
                    if (mat == null) mat = Material.BARRIER;
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(m.color(items.getString(k + ".displayname")));
                        List<String> lore = new ArrayList<>();
                        for (String s : items.getStringList(k + ".lore")) {
                            lore.add(m.color(s.replace("%price%", String.valueOf(items.getDouble(k + ".price")))));
                        }
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                    inv.setItem(items.getInt(k + ".slot"), item);
                }
            }
            p.openInventory(inv);
        } catch (Exception e) {
            p.sendMessage("§c[ArisShop] Lỗi cấu trúc file " + fileName);
        }
    }
                }
