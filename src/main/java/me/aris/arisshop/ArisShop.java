package me.aris.arisshop;

import me.aris.arisshop.commands.ShopCommand;
import me.aris.arisshop.listeners.ShopListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        String[] files = {"gui/maingui.yml", "gui/buy.yml", "shops/food.yml", "shops/gear.yml", "shops/end.yml", "shops/nether.yml"};
        for (String f : files) {
            File file = new File(getDataFolder(), f);
            if (!file.exists()) saveResource(f, false);
        }

        getCommand("arisshop").setExecutor(new ShopCommand());
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    public static ArisShop getInstance() { return instance; }
}
