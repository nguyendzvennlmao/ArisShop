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
        openFromFile(player, "gui/maingui.yml", "main-menu.categories");
    }

    public void openSubShop(Player player, String shopName) {
        openFromFile(player, "shops/" + shopName + ".yml", "items");
    }

    public void openBuyMenu(Player player) {
        openFromFile(player, "gui/buy.yml", "items");
    }

    private void openFromFile(Player player, String path, String section) {
        File file = new File(ArisShop.getInstance().getDataFolder(), path);
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        int rows = config.getInt("main-menu.rows", 3);
        String title = HexColor.format(config.getString("main-menu.title", "Shop"));
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        if (config.contains(section)) {
            for (String key : config.getConfigurationSection(section).getKeys(false)) {
                try {
                    String basePath = section + "." + key;
                    Material mat = Material.valueOf(config.getString(basePath + ".material").toUpperCase());
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(HexColor.format(config.getString(basePath + ".displayname", config.getString(basePath + ".display_name"))));
                    List<String> lore = new ArrayList<>();
                    for (String s : config.getStringList(basePath + ".lore")) lore.add(HexColor.format(s));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inv.setItem(config.getInt(basePath + ".slot"), item);
                } catch (Exception ignored) {}
            }
        }
        player.openInventory(inv);
    }
                             }
