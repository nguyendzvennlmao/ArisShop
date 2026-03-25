package me.aris.arisshop;

import me.aris.arisshop.commands.ShopCommand;
import me.aris.arisshop.listeners.ShopListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("gui/maingui.yml", false);
        saveResource("gui/buy.yml", false);
        saveResource("shops/food.yml", false);
        
        getCommand("arisshop").setExecutor(new ShopCommand());
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    public static ArisShop getInstance() { return instance; }
}
