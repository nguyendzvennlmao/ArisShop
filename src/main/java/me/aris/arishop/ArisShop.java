package me.aris.arishop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArisShop extends JavaPlugin implements TabCompleter {
    private static ArisShop instance;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        createShopFolder();
        setupEconomy();
        getCommand("shop").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
    }

    public void createShopFolder() {
        File folder = new File(getDataFolder(), "shop");
        if (!folder.exists()) folder.mkdirs();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) econ = rsp.getProvider();
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
        String s = getConfig().getString("sounds." + path);
        if (s == null || s.equalsIgnoreCase("none")) return;
        try { p.playSound(p.getLocation(), Sound.valueOf(s.toUpperCase()), 1f, 1f); } catch (Exception ignored) {}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("arishop.admin")) return true;
            reloadConfig();
            sender.sendMessage(color(getConfig().getString("messages.prefix") + "&aReloaded!"));
            return true;
        }
        if (sender instanceof Player) GuiManager.openMainMenu((Player) sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> h = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("arishop.admin")) h.add("reload");
        return h;
    }
  }
