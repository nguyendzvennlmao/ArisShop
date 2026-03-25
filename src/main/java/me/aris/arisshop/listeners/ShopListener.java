package me.aris.arisshop.listeners;

import me.aris.arisshop.ArisShop;
import me.aris.arisshop.models.CategoryInventory;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.io.File;

public class ShopListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().isEmpty()) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.contains("Shop") || title.contains("Categories")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            File file = new File(ArisShop.getInstance().getDataFolder(), "gui/maingui.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            if (config.contains("items")) {
                for (String key : config.getConfigurationSection("items").getKeys(false)) {
                    if (event.getSlot() == config.getInt("items." + key + ".slot")) {
                        String shopFile = config.getString("items." + key + ".open_shop");
                        if (shopFile != null) {
                            new CategoryInventory().openSubShop(player, shopFile);
                        }
                    }
                }
            }
        } else if (title.contains("Buying") || title.contains("Store")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            new CategoryInventory().openBuyMenu(player);
        }
    }
          }
