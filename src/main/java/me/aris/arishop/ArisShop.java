package me.aris.arishop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        createShopFolder();
        if (!setupEconomy()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
    }

    private void createShopFolder() {
        File shopFolder = new File(getDataFolder(), "shop");
        if (!shopFolder.exists()) {
            shopFolder.mkdirs();
        }
        if (getConfig().getConfigurationSection("main-menu.categories") != null) {
            for (String key : getConfig().getConfigurationSection("main-menu.categories").getKeys(false)) {
                String fileName = "shop/" + key + ".yml";
                if (getResource(fileName) != null) {
                    File outFile = new File(getDataFolder(), fileName);
                    if (!outFile.exists()) {
                        saveResource(fileName, false);
                    }
                }
            }
        }
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

    public void playSound(Player p, String path) {
        String soundName = getConfig().getString("sounds." + path);
        if (soundName == null || soundName.equalsIgnoreCase("none")) return;
        try {
            p.playSound(p.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1f, 1f);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("shop") && sender instanceof Player) {
            ShopListener.openMainMenu((Player) sender);
            return true;
        }
        return false;
    }
            }
