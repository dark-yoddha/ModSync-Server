package com.ModSync;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;
    private final java.util.Map<UUID, String> ignoredPlayers = new java.util.HashMap<>();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "ignoredplayers.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create ignoredplayers.yml!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        ignoredPlayers.clear();

        // Support legacy list format migration
        if (config.isList("ignored-players")) {
            List<String> list = config.getStringList("ignored-players");
            for (String s : list) {
                try {
                    ignoredPlayers.put(UUID.fromString(s), "Unknown");
                } catch (IllegalArgumentException e) {
                    // Ignore invalid UUIDs
                }
            }
            save(); // Save immediately to migrate to new format
            return;
        }

        // Load from configuration section
        org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("ignored-players");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = section.getString(key);
                    ignoredPlayers.put(uuid, name);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid UUIDs
                }
            }
        }
    }

    public void save() {
        if (config == null || file == null)
            return;

        // Clear existing to avoid potential conflicts/leftovers
        config.set("ignored-players", null);

        for (java.util.Map.Entry<UUID, String> entry : ignoredPlayers.entrySet()) {
            config.set("ignored-players." + entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save ignoredplayers.yml!");
            e.printStackTrace();
        }
    }

    public boolean isIgnored(UUID uuid) {
        return ignoredPlayers.containsKey(uuid);
    }

    public void addIgnored(UUID uuid, String name) {
        ignoredPlayers.put(uuid, name);
        save();
    }

    public void removeIgnored(UUID uuid) {
        ignoredPlayers.remove(uuid);
        save();
    }
}
