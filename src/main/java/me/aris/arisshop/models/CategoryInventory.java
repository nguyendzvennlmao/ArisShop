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
        openFromFile(player, "gui/maingui.yml", "main-menu.categories", true);
    }

    public void openSubShop(Player player, String shopName) {
        openFromFile(player, "shops/" + shopName + ".yml", "items", false);
    }

    public void openBuyMenu(Player player) {
        openFromFile(player, "gui/buy.yml", "items", false);
    }

    private void openFromFile(Player player, String path, String section, boolean isMain) {
        File file = new File(ArisShop.getInstance().getDataFolder(), path);
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        FileConfiguration mainConfig = ArisShop.getInstance().getConfig();
        
        int rows = isMain ? config.getInt("main-menu.rows", 3) : config.getInt("rows", 3);
        String titleKey = isMain ? "main-menu.title" : "title";
        Inventory inv = Bukkit.createInventory(null, rows * 9, HexColor.format(config.getString(titleKey, "Shop")));

        if (!isMain) {
            String bMat = mainConfig.getString("back-button.material", "RED_STAINED_GLASS_PANE");
            int bSlot = mainConfig.getInt("back-button.slot", 22);
            ItemStack back = new ItemStack(Material.valueOf(bMat.toUpperCase()));
            ItemMeta bMeta = back.getItemMeta();
            bMeta.setDisplayName(HexColor.format(mainConfig.getString("back-button.displayname")));
            List<String> bLore = new ArrayList<>();
            for (String s : mainConfig.getStringList("back-button.lore")) bLore.add(HexColor.format(s));
            bMeta.setLore(bLore);
            back.setItemMeta(bMeta);
            if (bSlot < inv.getSize()) inv.setItem(bSlot, back);
        }

        if (config.contains(section)) {
            for (String key : config.getConfigurationSection(section).getKeys(false)) {
                try {
                    String p = section + "." + key;
                    Material mat = Material.valueOf(config.getString(p + ".material").toUpperCase());
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    String name = config.contains(p + ".displayname") ? config.getString(p + ".displayname") : config.getString(p + ".display_name");
                    meta.setDisplayName(HexColor.format(name));
                    List<String> lore = new ArrayList<>();
                    for (String s : config.getStringList(p + ".lore")) lore.add(HexColor.format(s));
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
