package com.ModSync;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinListener implements Listener {

    private final JavaPlugin plugin;
    private final ConnectionEstablisher connectionEstablisher;
    private final ModSyncLogger modSyncLogger;
    private final DataManager dataManager;

    public JoinListener(JavaPlugin plugin, ConnectionEstablisher connectionEstablisher, ModSyncLogger modSyncLogger,
            DataManager dataManager) {
        this.plugin = plugin;
        this.connectionEstablisher = connectionEstablisher;
        this.modSyncLogger = modSyncLogger;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        FileConfiguration config = plugin.getConfig();
        int kickDelay = config.getInt("kick-delay", 3);
        long delayTicks = kickDelay * 20L;

        // Check after configured delay to allow channel registration to propagate
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline())
                return;

            if (dataManager.isIgnored(player.getUniqueId()))
                return;

            if (!connectionEstablisher.isVerified(player.getUniqueId())) {
                String message = config.getString("messages.not-installed", "§cModSync is required!").replace("\\n",
                        "\n");
                player.kickPlayer(message);
                modSyncLogger.log("Kicked " + player.getName() + " for missing ModSync");
            }
        }, delayTicks);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        connectionEstablisher.removeVerified(event.getPlayer().getUniqueId());
    }
}
