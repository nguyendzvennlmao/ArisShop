package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ConfirmPurchase {
    public static void open(Player p, ShopListener.ShopContext ctx) {
        ArisShop m = ArisShop.getInstance();
        Inventory inv = Bukkit.createInventory(null, 27, m.color("&8Xác nhận mua"));
        p.openInventory(inv);
    }
}
