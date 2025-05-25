package dev.crafty.core.util;

import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.plugin.CraftyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Configs {
    public static SectionWrapper getOrCreateConfigFile(CraftyPlugin plugin, String filePath) {
        File configFile = plugin.getDataPath().resolve(filePath).toFile();

        if (!configFile.exists()) {
            plugin.saveResource(filePath, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        return new SectionWrapper(config);
    }
}
