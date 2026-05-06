package me.aris.arishop;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.metadata.FixedMetadataValue;
import java.io.File;

public class ShopListener implements Listener {
    
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;
        ArisShop m = ArisShop.getInstance();
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.equals(m.color(m.getConfig().getString("main-menu.title")))) {
            e.setCancelled(true);
            ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
            for (String k : sec.getKeys(false)) {
                if (e.getSlot() == sec.getInt(k + ".slot")) {
                    p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                    ShopItem.open(p, k);
                    return;
                }
            }
            return;
        }

        File folder = new File(m.getDataFolder(), "shop");
        if (folder.exists() && folder.listFiles() != null) {
            for (File f : folder.listFiles()) {
                if (f == null || !f.getName().endsWith(".yml")) continue;
                YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
                if (title.equals(m.color(c.getString("title")))) {
                    e.setCancelled(true);
                    if (e.getSlot() == m.getConfig().getInt("gui.back-button.slot", 22)) {
                        p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                        ShopMain.open(p);
                        return;
                    }
                    ConfigurationSection items = c.getConfigurationSection("items");
                    if (items == null) return;
                    for (String k : items.getKeys(false)) {
                        if (e.getSlot() == items.getInt(k + ".slot")) {
                            p.setMetadata("aris_curr", new FixedMetadataValue(m, c.getString("currency", "MONEY")));
                            p.setMetadata("aris_file", new FixedMetadataValue(m, f.getName().replace(".yml", "")));
                            p.playSound(p.getLocation(), Sound.valueOf(m.getConfig().getString("sounds.button-click")), 1, 1);
                            ConfirmPurchase.open(p, items.getDouble(k + ".price"), e.getCurrentItem(), 1, items.getInt(k + ".stack", 64));
                            return;
                        }
                    }
                }
            }
        }
    }
}
