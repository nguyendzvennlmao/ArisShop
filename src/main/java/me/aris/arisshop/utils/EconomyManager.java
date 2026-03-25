package me.aris.arisshop.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private static Economy econ = null;

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public static boolean withdraw(Player player, double amount) {
        if (econ.getBalance(player) >= amount) {
            econ.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }
}
