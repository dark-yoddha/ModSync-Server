package com.ModSync;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PlayerLogger {

    private final JavaPlugin plugin;
    private final File playersFolder;

    public PlayerLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), "players");
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    public void logPlayer(Player player, List<String> currentMods, List<String> currentPacks) {
        File playerFile = new File(playersFolder, player.getName() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        // Handle Mods
        List<String> oldMods = config.getStringList("currently-installed-mods");
        List<String> deletedMods = new ArrayList<>(oldMods);
        deletedMods.removeAll(currentMods);

        config.set("currently-installed-mods", currentMods);
        config.set("deleted-mods", deletedMods);

        // Handle Resource Packs
        List<String> oldPacks = config.getStringList("currently-installed-resourcepacks");
        List<String> deletedPacks = new ArrayList<>(oldPacks);
        deletedPacks.removeAll(currentPacks);

        config.set("currently-installed-resourcepacks", currentPacks);
        config.set("deleted-resourcepacks", deletedPacks);

        // Add timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        config.set("last-updated", timestamp);

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player log for " + player.getName(), e);
        }
    }
}
