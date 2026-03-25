package me.aris.arisshop;

import me.aris.arisshop.commands.ShopCommand;
import me.aris.arisshop.listeners.ShopListener;
import me.aris.arisshop.utils.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        if (!EconomyManager.setupEconomy()) {
            getLogger().severe("Vault not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("arisshop").setExecutor(new ShopCommand());
        Bukkit.getPluginManager().registerEvents(new ShopListener(), this);
    }

    public static ArisShop getInstance() { return instance; }
}
