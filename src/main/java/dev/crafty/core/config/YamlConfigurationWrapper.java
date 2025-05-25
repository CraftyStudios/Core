package dev.crafty.core.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;

public class YamlConfigurationWrapper extends SectionWrapper {

    @Contract("null -> fail")
    public YamlConfigurationWrapper(YamlConfiguration config) {
        super(config);
    }

    public YamlConfiguration getYamlConfiguration() {
        return (YamlConfiguration) super.getDelegate();
    }
}
