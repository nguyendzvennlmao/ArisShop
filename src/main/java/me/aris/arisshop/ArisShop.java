package me.aris.arisshop;

import me.aris.arisshop.commands.ShopCommand;
import me.aris.arisshop.utils.*;
import me.aris.arisshop.shards.ArisShardsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;
    private ArisShardsManager shardsManager;
    private EconomyManager economyManager;
    private LangManager langManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.langManager = new LangManager(this);
        this.economyManager = new EconomyManager(this);
        this.shardsManager = new ArisShardsManager(this);
        
        getCommand("arisshop").setExecutor(new ShopCommand());
    }

    public static ArisShop getInstance() { return instance; }
    public ArisShardsManager getShardsManager() { return shardsManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public LangManager getLangManager() { return langManager; }
}
