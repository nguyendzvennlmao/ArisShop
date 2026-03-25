package me.aris.arisshop;

import me.aris.arisshop.commands.ShopCommand;
import me.aris.arisshop.shards.ArisShardsManager;
import me.aris.arisshop.utils.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class ArisShop extends JavaPlugin {
    private static ArisShop instance;
    private ArisShardsManager shardsManager;

    @Override
    public void onEnable() {
        instance = this;
        
        String[] resources = {
            "config.yml", "lang.yml", "shards.yml",
            "gui/maingui.yml", "gui/buy.yml",
            "shops/food.yml", "shops/gear.yml", "shops/end.yml", "shops/nether.yml"
        };
        
        for (String res : resources) {
            File file = new File(getDataFolder(), res);
            if (!file.exists()) {
                saveResource(res, false);
            }
        }

        this.shardsManager = new ArisShardsManager(this);
        getCommand("arisshop").setExecutor(new ShopCommand());
    }

    public static ArisShop getInstance() { return instance; }
    public ArisShardsManager getShardsManager() { return shardsManager; }
}
