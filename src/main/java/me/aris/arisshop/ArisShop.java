package me.aris.arisshop;

import me.aris.arisshop.utils.*;
import me.aris.arisshop.shards.ArisShardsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;
    private ArisShardsManager shardsManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.economyManager = new EconomyManager(this);
        this.shardsManager = new ArisShardsManager(this);
    }

    public static ArisShop getInstance() { return instance; }
    public ArisShardsManager getShardsManager() { return shardsManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
}
