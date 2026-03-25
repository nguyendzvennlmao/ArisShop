package me.aris.arisshop.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.entity.Player;
import me.aris.arisshop.ArisShop;

public class EconomyManager {
    private static Economy econ = null;

    public EconomyManager(ArisShop plugin) {
        setupEconomy();
    }

    private boolean setupEconomy() {
        if (ArisShop.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = ArisShop.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public boolean withdraw(Player player, double amount) {
        if (econ.getBalance(player) >= amount) {
            econ.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }
          }
