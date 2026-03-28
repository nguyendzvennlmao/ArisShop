package me.aris.arishop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.ArrayList;
import java.util.List;

public class ConfirmPurchase {
    public static void open(Player p, double price, ItemStack shopItem, int amount) {
        ArisShop m = ArisShop.getInstance();
        ConfigurationSection gui = m.getConfig().getConfigurationSection("gui.quantity-selector");
        Inventory inv = Bukkit.createInventory(null, gui.getInt("rows", 3) * 9, m.color(gui.getString("title")));
        p.setMetadata("aris_price", new FixedMetadataValue(m, price));
        p.setMetadata("aris_amount", new FixedMetadataValue(m, amount));
        p.setMetadata("aris_item", new FixedMetadataValue(m, shopItem));
        String[] buttons = {"confirm", "cancel", "add1", "add10", "set64", "remove1", "remove10", "remove64"};
        for (String b : buttons) {
            ConfigurationSection sec = gui.getConfigurationSection(b);
            if (sec != null) {
                ItemStack item = new ItemStack(Material.valueOf(sec.getString("material")));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) meta.setDisplayName(m.color(sec.getString("name")));
                item.setItemMeta(meta);
                inv.setItem(sec.getInt("slot"), item);
            }
        }
        ItemStack preview = shopItem.clone();
        ItemMeta pMeta = preview.getItemMeta();
        if (pMeta != null) {
            List<String> lore = new ArrayList<>();
            for (String s : gui.getStringList("item-preview.lore")) {
                lore.add(m.color(s.replace("%price%", m.format(price)).replace("%amount%", String.valueOf(amount)).replace("%total_price%", m.format(price * amount))));
            }
            pMeta.setLore(lore);
            preview.setItemMeta(pMeta);
        }
        inv.setItem(gui.getInt("item-preview.slot"), preview);
        p.openInventory(inv);
    }
                                                                }
