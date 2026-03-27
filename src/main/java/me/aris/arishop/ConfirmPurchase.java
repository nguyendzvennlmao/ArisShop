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

        createButton(inv, gui, "cancel", amount, price);
        createButton(inv, gui, "confirm", amount, price);
        createButton(inv, gui, "add1", 1, price);
        createButton(inv, gui, "add10", 10, price);
        createButton(inv, gui, "set64", 64, price);
        createButton(inv, gui, "remove1", 1, price);
        createButton(inv, gui, "remove10", 10, price);
        createButton(inv, gui, "remove64", 64, price);

        ItemStack preview = shopItem.clone();
        preview.setAmount(amount);
        ItemMeta meta = preview.getItemMeta();
        List<String> lore = new ArrayList<>();
        for (String s : gui.getStringList("item-preview.lore")) {
            lore.add(m.color(s.replace("%price%", String.valueOf(price))
                             .replace("%amount%", String.valueOf(amount))
                             .replace("%total_price%", String.valueOf(price * amount))));
        }
        meta.setLore(lore);
        preview.setItemMeta(meta);
        inv.setItem(gui.getInt("item-preview.slot"), preview);
        p.openInventory(inv);
    }

    private static void createButton(Inventory inv, ConfigurationSection sec, String key, int amount, double price) {
        ArisShop m = ArisShop.getInstance();
        ConfigurationSection b = sec.getConfigurationSection(key);
        ItemStack item = new ItemStack(Material.valueOf(b.getString("material")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(m.color(b.getString("name").replace("%amount%", String.valueOf(amount))));
        if (b.contains("lore")) {
            List<String> lore = new ArrayList<>();
            for (String s : b.getStringList("lore")) lore.add(m.color(s));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        inv.setItem(b.getInt("slot"), item);
    }
            }
