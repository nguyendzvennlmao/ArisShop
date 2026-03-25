package me.aris.arisshop.listeners;

import me.aris.arisshop.ArisShop;
import me.aris.arisshop.models.CategoryInventory;
import me.aris.arisshop.utils.HexColor;
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
        if (event.getView().getTitle().isEmpty() || event.getCurrentItem() == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        Player player = (Player) event.getWhoClicked();
        FileConfiguration mainConfig = ArisShop.getInstance().getConfig();
        String title = event.getView().getTitle();
        String clickSnd = mainConfig.getString("sounds.click-sound", "ui.button.click");
        Material backMat = Material.valueOf(mainConfig.getString("back-button.material", "RED_STAINED_GLASS_PANE").toUpperCase());
        
        event.setCancelled(true);

        if (event.getCurrentItem().getType() == backMat) {
            player.playSound(player.getLocation(), clickSnd, 1f, 1f);
            new CategoryInventory().openCategoryMenu(player);
            return;
        }

        File mainGuiFile = new File(ArisShop.getInstance().getDataFolder(), "gui/maingui.yml");
        FileConfiguration mainGuiConfig = YamlConfiguration.loadConfiguration(mainGuiFile);
        String mainTitle = HexColor.format(mainGuiConfig.getString("main-menu.title", ""));

        if (title.equalsIgnoreCase(mainTitle)) {
            for (String key : mainGuiConfig.getConfigurationSection("main-menu.categories").getKeys(false)) {
                if (event.getSlot() == mainGuiConfig.getInt("main-menu.categories." + key + ".slot")) {
                    player.playSound(player.getLocation(), clickSnd, 1f, 1f);
                    new CategoryInventory().openSubShop(player, mainGuiConfig.getString("main-menu.categories." + key + ".action"));
                    return;
                }
            }
        } else if (title.toLowerCase().contains("confirm") || title.toLowerCase().contains("buy")) {
            return;
        } else {
            player.playSound(player.getLocation(), clickSnd, 1f, 1f);
            new CategoryInventory().openBuyMenu(player);
        }
    }
            }
