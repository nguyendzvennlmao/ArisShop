package me.aris.arishop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.text.DecimalFormat;
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
        saveDefaultConfig();
        File shopFolder = new File(getDataFolder(), "shop");
        if (!shopFolder.exists()) {
            shopFolder.mkdirs();
            saveResource("shop/food.yml", false);
        }
        setupEconomy();
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
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

    public String format(double n) {
        if (n < 1000) return String.valueOf((int)n);
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
  }
