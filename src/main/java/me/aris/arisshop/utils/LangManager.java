package me.aris.arisshop.utils;

import me.aris.arisshop.ArisShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class LangManager {
    private final ArisShop plugin;
    private FileConfiguration langConfig;

    public LangManager(ArisShop plugin) {
        this.plugin = plugin;
        loadLang();
    }

    public void loadLang() {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) plugin.saveResource("lang.yml", false);
        this.langConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        return HexColor.format(langConfig.getString(path, ""));
    }
}
