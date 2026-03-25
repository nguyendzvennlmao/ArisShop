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
        openFromFile(player, "gui/maingui.yml", "main-menu.categories", "main-menu", true);
    }

    public void openSubShop(Player player, String shopName) {
        openFromFile(player, "shops/" + shopName + ".yml", "items", "shop-item-menu", false);
    }

    public void openBuyMenu(Player player) {
        openFromFile(player, "gui/buy.yml", "items", "main-menu", false);
    }

    private void openFromFile(Player player, String path, String section, String header, boolean isMain) {
        File file = new File(ArisShop.getInstance().getDataFolder(), path);
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        FileConfiguration mainConfig = ArisShop.getInstance().getConfig();
        
        int rows = config.getInt(header + ".rows", 3);
        Inventory inv = Bukkit.createInventory(null, rows * 9, HexColor.format(config.getString(header + ".title", "Shop")));

        if (!isMain && !path.contains("buy.yml")) {
            ItemStack back = new ItemStack(Material.valueOf(mainConfig.getString("back-button.material").toUpperCase()));
            ItemMeta bMeta = back.getItemMeta();
            bMeta.setDisplayName(HexColor.format(mainConfig.getString("back-button.displayname")));
            back.setItemMeta(bMeta);
            inv.setItem(mainConfig.getInt("back-button.slot"), back);
        }

        if (config.contains(section)) {
            for (String key : config.getConfigurationSection(section).getKeys(false)) {
                try {
                    String p = section + "." + key;
                    ItemStack item = new ItemStack(Material.valueOf(config.getString(p + ".material").toUpperCase()), config.getInt(p + ".amount", 1));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(HexColor.format(config.getString(p + ".displayname")));
                    
                    String price = config.getString(p + ".price", "0");
                    List<String> lore = new ArrayList<>();
                    for (String s : config.getStringList(p + ".lore")) {
                        lore.add(HexColor.format(s.replace("%price%", price)));
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inv.setItem(config.getInt(p + ".slot"), item);
                } catch (Exception ignored) {}
            }
        }
        player.openInventory(inv);
        String openSound = mainConfig.getString("sounds.open-sound", "");
        if (!openSound.isEmpty()) player.playSound(player.getLocation(), openSound, 1f, 1f);
    }
                }
