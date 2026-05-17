package com.coariz.coarizwildtp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoarizWildTPCommand implements CommandExecutor {

    private final CoarizWildTP plugin;

    public CoarizWildTPCommand(CoarizWildTP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(plugin.colorize("&6/coarizwildtp reload &7- Reloads the plugin configuration."));
            player.sendMessage(plugin.colorize("&6/coarizwildtp gui &7- Opens the dimension selection GUI."));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            if (!player.hasPermission("coarizwildtp.reload")) {
                player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("no_permission", "&cYou don't have permission to do that!")));
                return true;
            }

            plugin.reloadConfig();
            player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("reload_success", "&aConfiguration reloaded successfully!")));
            return true;
        }

        if (subCommand.equals("gui")) {
            if (!player.hasPermission("coarizwildtp.use")) {
                player.sendMessage(plugin.colorize(plugin.getLangConfig().getString("no_permission", "&cYou don't have permission to do that!")));
                return true;
            }

            if (plugin.getWildTPGUI() != null) {
                plugin.getWildTPGUI().openGUI(player);
            } else {
                player.sendMessage(plugin.colorize("&cGUI is not available."));
            }
            return true;
        }

        player.sendMessage(plugin.colorize("&cUnknown subcommand. Use /coarizwildtp for a list of commands."));
        return true;
    }
}
