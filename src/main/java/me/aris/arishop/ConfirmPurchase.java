package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ConfirmPurchase {
    public static void open(Player p, ShopListener.ShopContext ctx) {
        ArisShop m = ArisShop.getInstance();
        String title = m.color(m.getConfig().getString("gui.quantity-selector.title"));
        Inventory inv = Bukkit.createInventory(null, m.getConfig().getInt("gui.quantity-selector.rows") * 9, title);
        p.openInventory(inv);
    }
}
