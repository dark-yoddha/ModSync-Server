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

    public JoinListener(JavaPlugin plugin, ConnectionEstablisher connectionEstablisher, ModSyncLogger modSyncLogger) {
        this.plugin = plugin;
        this.connectionEstablisher = connectionEstablisher;
        this.modSyncLogger = modSyncLogger;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check 1 second after join to allow channel registration to propagate
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline())
                return;

            if (!connectionEstablisher.isVerified(player.getUniqueId())) {
                FileConfiguration config = plugin.getConfig();
                String message = config.getString("messages.not-installed", "§cModSync is required!");
                player.kickPlayer(message);
                modSyncLogger.log("Kicked " + player.getName() + " for missing ModSync");
            }
        }, 30L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        connectionEstablisher.removeVerified(event.getPlayer().getUniqueId());
    }
}
