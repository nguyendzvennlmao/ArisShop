package me.aris.arishop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    public static class ShopContext { String cat, item; int amount = 1; double price; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;
        ArisShop m = ArisShop.getInstance();
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();

        if (title.equals(m.color(m.getConfig().getString("main-menu.title")))) {
            e.setCancelled(true);
            ConfigurationSection sec = m.getConfig().getConfigurationSection("main-menu.categories");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    if (e.getSlot() == sec.getInt(key + ".slot")) {
                        ShopItem.open(p, key);
                        return;
                    }
                }
            }
        }
    }
}
