package com.ModSync;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String url;
    private final String currentPluginVersion;
    private String latestPluginVersion = null; // Stores the latest version from the web
    private boolean updateAvailable = false; // Flag to indicate if an update is available

    public UpdateChecker(JavaPlugin plugin, String url) {
        this.plugin = plugin;
        this.url = url;
        // Get the current plugin version from the plugin.yml file
        this.currentPluginVersion = plugin.getDescription().getVersion();
    }

    public void check() {
        // Run the check on a new thread to avoid blocking the server
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Get the server's version string (e.g., "1.20.4")
                String serverVersion = Bukkit.getBukkitVersion().split("-")[0];

                // Create a connection to the URL
                URL versionsUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) versionsUrl.openConnection();
                connection.setRequestProperty("User-Agent", "ModSync-UpdateChecker"); // Set a user agent to avoid
                                                                                      // issues

                // Read the file content into a String
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                }

                // Parse the JSON-like string manually
                Map<String, String> versionsMap = parseJsonString(content.toString());

                String latestVersion = null;
                // Iterate through the keys (the version ranges) to find a match
                for (String range : versionsMap.keySet()) {
                    if (isVersionInRange(serverVersion, range)) {
                        latestVersion = versionsMap.get(range);
                        break;
                    }
                }

                if (latestVersion == null) {
                    // No latest version found for the current server's Minecraft version
                    plugin.getLogger().log(Level.WARNING, "Could not find a recommended version for Minecraft {0}.",
                            serverVersion);
                    return;
                }

                // Compare versions to see if an update is available
                if (isNewerVersion(latestVersion, currentPluginVersion)) {
                    this.latestPluginVersion = latestVersion;
                    this.updateAvailable = true;
                    plugin.getLogger().log(Level.INFO, "A new version of ModSync is available! ({0})", latestVersion);
                } else {
                    plugin.getLogger().log(Level.INFO, "You are running the latest version of ModSync ({0}).",
                            latestVersion);
                }

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Could not check for updates. Error: " + e.getMessage());
            }
        });
    }

    /**
     * @return true if an update is available, false otherwise.
     */
    public boolean isUpdateAvailable() {
        return this.updateAvailable;
    }

    /**
     * @return The latest plugin version string from the web.
     */
    public String getLatestPluginVersion() {
        return this.latestPluginVersion;
    }

    /**
     * Parses a simple JSON-like string into a Map.
     * This is a simple implementation for a flat key-value structure.
     * 
     * @param jsonString The JSON-like string.
     * @return A Map representing the key-value pairs.
     */
    private Map<String, String> parseJsonString(String jsonString) {
        Map<String, String> map = new HashMap<>();
        // Remove brackets and split by commas
        String content = jsonString.trim().substring(1, jsonString.length() - 1);
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Checks if a given version string is within a specified range string.
     *
     * @param version The version string to check (e.g., "1.21.1").
     * @param range   The range string (e.g., "1.21-1.21.4").
     * @return true if the version is within the range, false otherwise.
     */
    private boolean isVersionInRange(String version, String range) {
        // Handle single version ranges (e.g., "1.21")
        if (!range.contains("-")) {
            return version.equalsIgnoreCase(range);
        }

        String[] parts = range.split("-");
        if (parts.length != 2) {
            return false;
        }

        String minVersion = parts[0];
        String maxVersion = parts[1];

        return !isNewerVersion(minVersion, version) && !isNewerVersion(version, maxVersion);
    }

    /**
     * Compares two version strings (e.g., "1.2.0" and "1.1.5") numerically.
     *
     * @param newVersionString     The version string to check against.
     * @param currentVersionString The current version string.
     * @return true if the new version is greater, false otherwise.
     */
    private boolean isNewerVersion(String newVersionString, String currentVersionString) {
        // Split versions by the dot
        String[] newParts = newVersionString.split("\\.");
        String[] currentParts = currentVersionString.split("\\.");

        int length = Math.max(newParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            // Get the integer value of each part, defaulting to 0 if the part is missing
            int newPart = (i < newParts.length) ? Integer.parseInt(newParts[i]) : 0;
            int currentPart = (i < currentParts.length) ? Integer.parseInt(currentParts[i]) : 0;

            if (newPart > currentPart) {
                return true;
            } else if (newPart < currentPart) {
                return false;
            }
        }

        // If all parts are equal, the versions are the same
        return false;
    }
}
