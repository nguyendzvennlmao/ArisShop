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
        openGeneric(player, "gui/maingui.yml");
    }

    public void openSubShop(Player player, String shopName) {
        openGeneric(player, "shops/" + shopName);
    }

    public void openBuyMenu(Player player) {
        openGeneric(player, "gui/buy.yml");
    }

    private void openGeneric(Player player, String path) {
        File file = new File(ArisShop.getInstance().getDataFolder(), path);
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        Inventory inv = Bukkit.createInventory(null, config.getInt("size", 27), HexColor.format(config.getString("title")));
        if (config.contains("items")) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                try {
                    Material mat = Material.valueOf(config.getString("items." + key + ".material").toUpperCase());
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(HexColor.format(config.getString("items." + key + ".display_name")));
                    List<String> lore = new ArrayList<>();
                    for (String s : config.getStringList("items." + key + ".lore")) lore.add(HexColor.format(s));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inv.setItem(config.getInt("items." + key + ".slot"), item);
                } catch (Exception ignored) {}
            }
        }
        player.openInventory(inv);
    }
                 }
