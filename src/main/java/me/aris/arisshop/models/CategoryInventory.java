package me.aris.arisshop.models;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CategoryInventory {
    public void openCategoryMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0Aris Network - Shop");
        
        ItemStack decor = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = decor.getItemMeta();
        meta.setDisplayName(" ");
        decor.setItemMeta(meta);
        
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, decor);
        }
        
        player.openInventory(inv);
    }
}
