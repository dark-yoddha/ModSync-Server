package com.ModSync;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

        @Override
        public void onEnable() {
                getLogger().info("ModSync enabled!");

                // Save default config
                saveDefaultConfig();

                // Initialize Loggers
                PlayerLogger playerLogger = new PlayerLogger(this);
                ModSyncLogger modSyncLogger = new ModSyncLogger(this);

                // Create ConnectionEstablisher
                ConnectionEstablisher connectionEstablisher = new ConnectionEstablisher(this, getLogger(), playerLogger,
                                modSyncLogger);

                // Register events
                getServer().getPluginManager().registerEvents(
                                new JoinListener(this, connectionEstablisher, modSyncLogger),
                                this);

                // Register commands
                getCommand("modsync").setExecutor(new ModSyncCommand(this, connectionEstablisher));

                // Register the incoming channel
                this.getServer().getMessenger().registerIncomingPluginChannel(this, "modsync:connection",
                                connectionEstablisher);

                // Register the outgoing channel (optional, but good practice)
                this.getServer().getMessenger().registerOutgoingPluginChannel(this, "modsync:connection");

                // Check for updates
                // TODO: Replace with your actual GitHub raw file URL
                UpdateChecker updateChecker = new UpdateChecker(this,
                                "https://raw.githubusercontent.com/dark-yoddha/ModSync-Server/refs/heads/ModSync-Utility/versions.json");
                updateChecker.check();
        }

        @Override
        public void onDisable() {
                getLogger().info("ModSync disabled!");
        }
}