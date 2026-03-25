package me.aris.arisshop;

import me.aris.arisshop.commands.ShopCommand;
import me.aris.arisshop.utils.*;
import me.aris.arisshop.shards.ArisShardsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        getCommand("arisshop").setExecutor(new ShopCommand());
        
        getLogger().info("ArisShop da kich hoat lenh /shop!");
    }

    public static ArisShop getInstance() { return instance; }
}
