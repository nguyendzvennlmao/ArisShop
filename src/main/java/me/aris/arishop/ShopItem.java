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
    public static void open(Player p, String shopName) {
        ArisShop m = ArisShop.getInstance();
        File f = new File(m.getDataFolder() + "/shop", shopName.replace(".yml", "") + ".yml");
        if (!f.exists()) return;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
        Inventory inv = Bukkit.createInventory(null, c.getInt("rows", 3) * 9, m.color(c.getString("title")));
        
        ConfigurationSection items = c.getConfigurationSection("items");
        if (items != null) {
            for (String k : items.getKeys(false)) {
                ItemStack item = new ItemStack(Material.valueOf(items.getString(k + ".material")));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(m.color(items.getString(k + ".displayname")));
                List<String> lore = new ArrayList<>();
                String priceStr = m.format(items.getDouble(k + ".price"));
                for (String s : items.getStringList(k + ".lore")) lore.add(m.color(s.replace("%price%", priceStr)));
                meta.setLore(lore);
                item.setItemMeta(meta);
                inv.setItem(items.getInt(k + ".slot"), item);
            }
        }

        ConfigurationSection backCfg = m.getConfig().getConfigurationSection("gui.back-button");
        if (backCfg != null) {
            ItemStack back = new ItemStack(Material.valueOf(backCfg.getString("material", "BARRIER")));
            ItemMeta bMeta = back.getItemMeta();
            bMeta.setDisplayName(m.color(backCfg.getString("displayname")));
            back.setItemMeta(bMeta);
            inv.setItem(backCfg.getInt("slot", 22), back);
        }
        p.openInventory(inv);
    }
            }
