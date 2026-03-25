package me.aris.arisshop.models;

import me.aris.arisshop.ArisShop;
import me.aris.arisshop.utils.HexColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CategoryInventory {
    public void openCategoryMenu(Player player) {
        File file = new File(ArisShop.getInstance().getDataFolder(), "gui/maingui.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        String title = HexColor.format(config.getString("title", "&0Shop"));
        int size = config.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        if (config.contains("items")) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                int slot = config.getInt("items." + key + ".slot");
                String matName = config.getString("items." + key + ".material");
                if (matName == null) continue;
                
                ItemStack item = new ItemStack(Material.valueOf(matName.toUpperCase()));
                ItemMeta meta = item.getItemMeta();
                
                meta.setDisplayName(HexColor.format(config.getString("items." + key + ".display_name")));
                List<String> lore = new ArrayList<>();
                for (String line : config.getStringList("items." + key + ".lore")) {
                    lore.add(HexColor.format(line));
                }
                meta.setLore(lore);
                
                item.setItemMeta(meta);
                inv.setItem(slot, item);
            }
        }
        player.openInventory(inv);
    }
        }
