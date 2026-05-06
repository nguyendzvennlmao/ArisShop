package me.aris.arishop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;
    private static Economy econ = null;
    private boolean isFolia;

    @Override
    public void onEnable() {
        instance = this;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }
        if (!setupEconomy()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        
        File shopFolder = new File(getDataFolder(), "shop");
        if (!shopFolder.exists()) {
            shopFolder.mkdirs();
            List<String> defaultShops = Arrays.asList("gear.yml", "shards.yml", "end.yml", "nether.yml", "food.yml");
            for (String fileName : defaultShops) {
                File file = new File(shopFolder, fileName);
                if (!file.exists()) {
                    saveResource("shop/" + fileName, false);
                }
            }
        }
        
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getServer().getPluginManager().registerEvents(new ConfirmPurchase(), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public static ArisShop getInstance() { return instance; }
    public static Economy getEconomy() { return econ; }

    public String color(String msg) {
        if (msg == null) return "";
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            String color = msg.substring(matcher.start(), matcher.end());
            msg = msg.replace(color, net.md_5.bungee.api.ChatColor.of(color.substring(1)).toString());
            matcher = pattern.matcher(msg);
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String format(double n) {
        if (n < 1000) return String.valueOf((long)n);
        ConfigurationSection f = getConfig().getConfigurationSection("amount-format");
        String[] units = {"", f.getString("k"), f.getString("m"), f.getString("b"), f.getString("t")};
        int exp = (int) (Math.log10(n) / 3);
        return new DecimalFormat("#.#").format(n / Math.pow(10, exp * 3)) + units[exp];
    }

    public void sendMsg(Player p, String path, String... replace) {
        ConfigurationSection sec = getConfig().getConfigurationSection("messages." + path);
        if (sec == null) return;
        String prefix = color(getConfig().getString("messages.prefix", ""));
        String text = sec.getString("text", "");
        for (int i = 0; i < replace.length; i += 2) text = text.replace(replace[i], replace[i+1]);
        String finalMsg = prefix + color(text);
        if (sec.getBoolean("chat")) p.sendMessage(finalMsg);
        if (sec.getBoolean("actionbar")) p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(finalMsg));
    }

    public void runTask(Player p, Runnable r) {
        if (isFolia) p.getScheduler().run(this, task -> r.run(), null);
        else Bukkit.getScheduler().runTask(this, r);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) ShopMain.open(p);
        return true;
    }

    public class ConfirmPurchase implements Listener {
        
        public void open(Player p, double price, ItemStack shopItem, int amount, int maxStack) {
            ConfigurationSection gui = getConfig().getConfigurationSection("gui.quantity-selector");
            Inventory inv = Bukkit.createInventory(null, gui.getInt("rows", 3) * 9, color(gui.getString("title")));
            
            p.setMetadata("aris_price", new FixedMetadataValue(ArisShop.this, price));
            p.setMetadata("aris_amount", new FixedMetadataValue(ArisShop.this, amount));
            p.setMetadata("aris_item", new FixedMetadataValue(ArisShop.this, shopItem));
            p.setMetadata("aris_stack", new FixedMetadataValue(ArisShop.this, maxStack));
            
            addButton(inv, gui, "confirm");
            addButton(inv, gui, "cancel");
            
            if (maxStack == 1) {
            }
            else if (maxStack == 16) {
                if (amount < 16) {
                    addButton(inv, gui, "add1");
                    if (amount + 10 <= 16) {
                        addButton(inv, gui, "add10");
                    }
                }
                if (amount > 1) {
                    addButton(inv, gui, "remove1");
                }
                if (amount > 10) {
                    addButton(inv, gui, "remove10");
                }
            }
            else if (maxStack == 64) {
                if (amount < 64) {
                    addButton(inv, gui, "add1");
                    if (amount + 10 <= 64) {
                        addButton(inv, gui, "add10");
                    }
                    if (amount + 64 <= 64) {
                        addButton(inv, gui, "set64");
                    }
                }
                if (amount > 1) {
                    addButton(inv, gui, "remove1");
                }
                if (amount > 10) {
                    addButton(inv, gui, "remove10");
                }
            }
            
            ItemStack preview = shopItem.clone();
            preview.setAmount(amount);
            ItemMeta pMeta = preview.getItemMeta();
            if (pMeta != null) {
                List<String> lore = new ArrayList<>();
                for (String s : gui.getStringList("item-preview.lore")) {
                    lore.add(color(s.replace("%amount%", String.valueOf(amount))
                                     .replace("%total_price%", format(price * amount))));
                }
                pMeta.setLore(lore);
                preview.setItemMeta(pMeta);
            }
            inv.setItem(gui.getInt("item-preview.slot"), preview);
            
            p.openInventory(inv);
        }
        
        private void addButton(Inventory inv, ConfigurationSection gui, String buttonPath) {
            ConfigurationSection sec = gui.getConfigurationSection(buttonPath);
            if (sec != null) {
                try {
                    ItemStack item = new ItemStack(Material.valueOf(sec.getString("material")));
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && sec.contains("name")) {
                        meta.setDisplayName(color(sec.getString("name")));
                    }
                    item.setItemMeta(meta);
                    inv.setItem(sec.getInt("slot"), item);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Invalid material for button: " + buttonPath);
                }
            }
        }
        
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player p = (Player) event.getWhoClicked();
            String title = event.getView().getTitle();
            ConfigurationSection gui = getConfig().getConfigurationSection("gui.quantity-selector");
            if (title == null || !title.equals(color(gui.getString("title")))) return;
            
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            if (!p.hasMetadata("aris_price") || !p.hasMetadata("aris_amount") || 
                !p.hasMetadata("aris_item") || !p.hasMetadata("aris_stack")) return;
            
            double price = p.getMetadata("aris_price").get(0).asDouble();
            int currentAmount = p.getMetadata("aris_amount").get(0).asInt();
            ItemStack shopItem = (ItemStack) p.getMetadata("aris_item").get(0).value();
            int maxStack = p.getMetadata("aris_stack").get(0).asInt();
            
            String displayName = clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "";
            
            if (displayName.contains(color(gui.getString("confirm.name")))) {
                double totalPrice = price * currentAmount;
                if (econ.has(p, totalPrice)) {
                    econ.withdrawPlayer(p, totalPrice);
                    ItemStack toGive = shopItem.clone();
                    toGive.setAmount(currentAmount);
                    p.getInventory().addItem(toGive);
                    p.closeInventory();
                    p.sendMessage(color("&aĐã mua thành công " + currentAmount + " với giá " + format(totalPrice)));
                } else {
                    p.sendMessage(color("&cBạn không đủ tiền!"));
                    p.closeInventory();
                }
            }
            else if (displayName.contains(color(gui.getString("cancel.name")))) {
                p.closeInventory();
                p.sendMessage(color("&cĐã hủy giao dịch"));
            }
            else if (displayName.contains(color(gui.getString("add1.name")))) {
                int newAmount = Math.min(currentAmount + 1, maxStack);
                if (newAmount != currentAmount) {
                    open(p, price, shopItem, newAmount, maxStack);
                }
            }
            else if (displayName.contains(color(gui.getString("add10.name")))) {
                int newAmount = Math.min(currentAmount + 10, maxStack);
                if (newAmount != currentAmount) {
                    open(p, price, shopItem, newAmount, maxStack);
                }
            }
            else if (displayName.contains(color(gui.getString("set64.name")))) {
                int newAmount = Math.min(currentAmount + 64, maxStack);
                if (newAmount != currentAmount) {
                    open(p, price, shopItem, newAmount, maxStack);
                }
            }
            else if (displayName.contains(color(gui.getString("remove1.name")))) {
                int newAmount = Math.max(currentAmount - 1, 1);
                if (newAmount != currentAmount) {
                    open(p, price, shopItem, newAmount, maxStack);
                }
            }
            else if (displayName.contains(color(gui.getString("remove10.name")))) {
                int newAmount = Math.max(currentAmount - 10, 1);
                if (newAmount != currentAmount) {
                    open(p, price, shopItem, newAmount, maxStack);
                }
            }
        }
    }
    }
