# CraftyCore Configuration Library

A comprehensive, modular, API-driven configuration solution for Spigot plugins.

## Features

- Annotation-based configuration system
- Support for complex type serialization
- Built-in serializers for common types
- Configuration reloading support via API and commands
- Easy to use API
- Fully documented

## Getting Started

### Adding CraftyCore as a dependency

Add CraftyCore as a dependency in your plugin.yml:

```yaml
depend: [CraftyCore]
```

### Creating a configuration class

Create a class with the `@ConfigurationFile` annotation to specify which configuration file to use:

```java
@ConfigurationFile(file = "config.yml")
public class MyConfig {

    @ConfigValue(key = "server.name", comment = "The name of the server")
    private String serverName = "My Server";

    @ConfigValue(key = "server.max-players", comment = "The maximum number of players")
    private int maxPlayers = 100;

    // Constructor
    public MyConfig() {
        // Load configuration values
        ConfigurationUtils.load(this);
    }

    // Getters and setters
    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    // Save configuration values
    public void saveConfig() {
        ConfigurationUtils.save(this);
    }
}
```

### Using the configuration class

```java
public class MyPlugin extends JavaPlugin {

    private MyConfig config;

    @Override
    public void onEnable() {
        // Create the configuration
        config = new MyConfig();

        // Use the configuration values
        getLogger().info("Server name: " + config.getServerName());
        getLogger().info("Max players: " + config.getMaxPlayers());
    }
}
```

### Reloading configurations

CraftyCore provides two ways to reload configurations:

#### 1. Manual reloading

You can add a method to your configuration class to reload its values:

```java
public void reloadConfig() {
    ConfigurationUtils.reload(this);
}
```

Then call this method whenever you need to reload the configuration:

```java
config.reloadConfig();
```

#### 2. Automatic reloading with commands

CraftyCore provides a command to reload configurations: `/craftycore reload [plugin]`

To use this feature, register your configuration object with CraftyCore:

```java
// In your plugin's onEnable method
CraftyCore craftyCore = (CraftyCore) getServer().getPluginManager().getPlugin("CraftyCore");
if (craftyCore != null) {
    craftyCore.registerConfig(this, config);
}
```

Or create your configuration object with a constructor that takes a Plugin parameter:

```java
public MyConfig(Plugin plugin) {
    // Load configuration values
    ConfigurationUtils.load(this);

    // Register with CraftyCore for automatic reloading
    CraftyCore craftyCore = (CraftyCore) plugin.getServer().getPluginManager().getPlugin("CraftyCore");
    if (craftyCore != null) {
        craftyCore.registerConfig(plugin, this);
    }
}
```

## Creating custom serializers

You can create custom serializers for complex types by implementing the `ConfigSerializer` interface:

```java
public class MyCustomSerializer implements ConfigSerializer<MyCustomType> {

    @Override
    public void serialize(MyCustomType value, ConfigurationSection section, String path) {
        // Serialize the value to the configuration section
    }

    @Override
    public MyCustomType deserialize(ConfigurationSection section, String path) {
        // Deserialize the value from the configuration section
    }

    @Override
    public Class<MyCustomType> getType() {
        return MyCustomType.class;
    }
}
```

### Registering custom serializers

Register your custom serializer with the configuration system:

```java
ConfigurationUtils.registerSerializer(new MyCustomSerializer());
```

## Built-in serializers

CraftyCore comes with built-in serializers for common types:

- Primitive types (String, Integer, Boolean, Double, Float, Long)
- Lists of primitive types
- ItemStack
- Location

## Configuration annotations

### @ConfigurationFile

The `@ConfigurationFile` annotation is used to specify which configuration file to use for a class:

```java
@ConfigurationFile(file = "config.yml")
public class MyConfig {
    // ...
}
```

#### Parameters

- `file`: The path to the configuration file, relative to the plugin's data folder
- `createIfNotExists`: Whether to create the file if it doesn't exist (default: true)
- `autoSave`: Whether to automatically save changes to the file (default: false)

### @ConfigValue

The `@ConfigValue` annotation is used to specify which configuration key to use for a field:

```java
@ConfigValue(key = "server.name", comment = "The name of the server")
private String serverName = "My Server";
```

#### Parameters

- `key`: The configuration key path
- `required`: Whether the value is required (default: true)
- `defaultValue`: The default value to use if the key is not found (default: "")
- `autoUpdate`: Whether to automatically update the configuration file with changes to this field (default: false)
- `comment`: A comment to include in the configuration file for this key (default: "")

## License

This project is licensed under the MIT License - see the LICENSE file for details.
