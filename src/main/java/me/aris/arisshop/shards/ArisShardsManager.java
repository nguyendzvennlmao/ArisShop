package me.aris.arisshop.shards;

import me.aris.arisshop.ArisShop;
import org.bukkit.entity.Player;

public class ArisShardsManager {
    private final ArisShop plugin;

    public ArisShardsManager(ArisShop plugin) {
        this.plugin = plugin;
    }

    public boolean consumeShards(Player player, int amount) {
        return true; 
    }
}
