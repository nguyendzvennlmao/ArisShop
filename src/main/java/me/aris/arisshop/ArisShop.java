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
        
        String[] resources = {"gui/maingui.yml", "gui/buy.yml", "shops/food.yml", "shops/gear.yml", "shops/end.yml", "shops/nether.yml"};
        for (String res : resources) {
            File f = new File(getDataFolder(), res);
            if (!f.exists()) saveResource(res, false);
        }

        getCommand("arisshop").setExecutor(new ShopCommand());
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    public static ArisShop getInstance() { return instance; }
}
