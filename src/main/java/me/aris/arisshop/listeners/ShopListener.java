package me.aris.arisshop.listeners;

import me.aris.arisshop.ArisShop;
import me.aris.arisshop.models.CategoryInventory;
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
        if (event.getView().getTitle().isEmpty() || event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.contains("ѕʜᴏᴘ")) {
            event.setCancelled(true);
            File file = new File(ArisShop.getInstance().getDataFolder(), "gui/maingui.yml");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            if (config.contains("main-menu.categories")) {
                for (String key : config.getConfigurationSection("main-menu.categories").getKeys(false)) {
                    if (event.getSlot() == config.getInt("main-menu.categories." + key + ".slot")) {
                        String action = config.getString("main-menu.categories." + key + ".action");
                        if (action != null) new CategoryInventory().openSubShop(player, action);
                        return;
                    }
                }
            }
        } else if (!title.contains("Confirm")) {
            event.setCancelled(true);
            new CategoryInventory().openBuyMenu(player);
        }
    }
                }
