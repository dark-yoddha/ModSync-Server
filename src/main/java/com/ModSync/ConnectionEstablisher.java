package com.ModSync;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ConnectionEstablisher implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final PlayerLogger playerLogger;
    private final ModSyncLogger modSyncLogger;
    private final java.util.Set<java.util.UUID> verifiedPlayers = new java.util.HashSet<>();
    private final java.util.Map<java.util.UUID, java.util.List<String>> playerMods = new java.util.HashMap<>();
    private final java.util.Map<java.util.UUID, java.util.List<String>> playerPacks = new java.util.HashMap<>();

    public ConnectionEstablisher(JavaPlugin plugin, Logger logger, PlayerLogger playerLogger,
            ModSyncLogger modSyncLogger) {
        this.plugin = plugin;
        this.logger = logger;
        this.playerLogger = playerLogger;
        this.modSyncLogger = modSyncLogger;
    }

    public boolean isVerified(java.util.UUID uuid) {
        return verifiedPlayers.contains(uuid);
    }

    public void removeVerified(java.util.UUID uuid) {
        verifiedPlayers.remove(uuid);
        playerMods.remove(uuid);
        playerPacks.remove(uuid);
    }

    public java.util.List<String> getMods(java.util.UUID uuid) {
        return playerMods.get(uuid);
    }

    public java.util.List<String> getPacks(java.util.UUID uuid) {
        return playerPacks.get(uuid);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("modsync:connection")) {
            return;
        }

        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(message);
                java.io.DataInputStream dis = new java.io.DataInputStream(bais)) {

            String data = readString(dis);
            // logger.info("Received ModSync data from " + player.getName() + ": " + data);

            // Parse data: modList|packList
            String[] parts = data.split("\\|");
            if (parts.length < 2)
                return; // Malformed

            String modListStr = parts[0];
            String packListStr = parts[1];

            java.util.List<String> clientMods = java.util.Arrays.asList(modListStr.split(","));
            java.util.List<String> clientPacks = java.util.Arrays.asList(packListStr.split(","));

            FileConfiguration config = plugin.getConfig();
            java.util.List<String> bannedMods = config.getStringList("banned-mods");
            java.util.List<String> bannedPacks = config.getStringList("banned-resourcepacks");

            // Check for banned mods
            for (String mod : clientMods) {
                if (bannedMods.contains(mod)) {
                    String msg = config.getString("messages.banned-mod", "Banned mod: %s");
                    final String kickMsg = String.format(msg, mod);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.kickPlayer(kickMsg);
                        modSyncLogger.log("Kicked " + player.getName() + " for banned mod: " + mod);
                    });
                    return;
                }
            }

            // Check for banned packs
            for (String pack : clientPacks) {
                if (bannedPacks.contains(pack)) {
                    String msg = config.getString("messages.banned-pack", "Banned pack: %s");
                    final String kickMsg = String.format(msg, pack);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.kickPlayer(kickMsg);
                        modSyncLogger.log("Kicked " + player.getName() + " for banned resource pack: " + pack);
                    });
                    return;
                }
            }

            // If we get here, the player is verified
            verifiedPlayers.add(player.getUniqueId());
            playerMods.put(player.getUniqueId(), clientMods);
            playerPacks.put(player.getUniqueId(), clientPacks);

            modSyncLogger.log("Connection established with " + player.getName());

            // Schedule logging after 2 minutes (2400 ticks)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    playerLogger.logPlayer(player, clientMods, clientPacks);
                }
            }, 2400L);

        } catch (Exception e) {
            logger.severe("Failed to read ModSync payload from " + player.getName());
            e.printStackTrace();
        }
    }

    // Helper to read VarInt-prefixed UTF-8 string
    private String readString(java.io.DataInputStream dis) throws java.io.IOException {
        int len = readVarInt(dis);
        byte[] bytes = new byte[len];
        dis.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private int readVarInt(java.io.DataInputStream dis) throws java.io.IOException {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = dis.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }
}
