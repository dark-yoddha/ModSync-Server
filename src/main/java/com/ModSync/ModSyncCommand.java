package com.ModSync;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ModSyncCommand implements TabExecutor {

    private final JavaPlugin plugin;
    private final ConnectionEstablisher connectionEstablisher;

    public ModSyncCommand(JavaPlugin plugin, ConnectionEstablisher connectionEstablisher) {
        this.plugin = plugin;
        this.connectionEstablisher = connectionEstablisher;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("modsync.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /modsync <modlist|packlist|reload> [player]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "ModSync configuration reloaded.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /modsync " + subCommand + " <player>");
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID uuid = target.getUniqueId();

        if (subCommand.equals("modlist")) {
            List<String> mods = connectionEstablisher.getMods(uuid);
            if (mods == null) {
                sender.sendMessage(ChatColor.RED + "No mod data available for " + playerName);
                return true;
            }

            String filteredMods = mods.stream()
                    .filter(mod -> (!mod.startsWith("fabric-") || mod.equals("fabric-api"))
                            && !mod.equals("fabricloader") && !mod.equals("java")
                            && !mod.equals("minecraft"))
                    .collect(Collectors.joining(", "));

            sender.sendMessage(ChatColor.GREEN + "[ModSync] " + playerName + "'s modlist:");
            sender.sendMessage(ChatColor.GREEN + filteredMods);

        } else if (subCommand.equals("packlist")) {
            List<String> packs = connectionEstablisher.getPacks(uuid);
            if (packs == null) {
                sender.sendMessage(ChatColor.RED + "No resource pack data available for " + playerName);
                return true;
            }

            String filteredPacks = packs.stream()
                    .filter(pack -> !pack.equals("vanilla") && !pack.equals("fabric") && !pack.startsWith("fabric-"))
                    .collect(Collectors.joining(", "));

            sender.sendMessage(ChatColor.YELLOW + "[ModSync] " + playerName + "'s packlist:");
            sender.sendMessage(ChatColor.YELLOW + filteredPacks);

        } else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("modlist", "packlist", "reload");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("modlist") || args[0].equalsIgnoreCase("packlist"))) {
            return null; // Return null to default to online player names
        }
        return Collections.emptyList();
    }
}
