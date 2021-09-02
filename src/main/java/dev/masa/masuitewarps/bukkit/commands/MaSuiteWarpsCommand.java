package dev.masa.masuitewarps.bukkit.commands;

import dev.masa.masuitecore.core.adapters.BukkitAdapter;
import dev.masa.masuitecore.core.channels.BukkitPluginChannel;
import dev.masa.masuitewarps.bukkit.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jin_ou
 */
public class MaSuiteWarpsCommand implements TabCompleter, CommandExecutor {
    private static final MaSuiteWarps PLUGIN = MaSuiteWarps.getPlugin();
    private static final Pattern WARP_NAME_PATTERN = Pattern.compile("^[_a-zA-Z0-9\\u4e00-\\u9fa5]{0,16}$");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ("setwarp".equalsIgnoreCase(label)) {
            return setwarpCmd(sender, args);
        }
        if ("warp".equalsIgnoreCase(label)) {
            return warpCmd(sender, args);
        }
        if ("delwarp".equalsIgnoreCase(label)) {
            return delwarpCmd(sender, args);
        }
        if ("warps".equalsIgnoreCase(label)) {
            return listWarpCmd(sender, args);
        }

        return false;
    }

    private boolean listWarpCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        new BukkitPluginChannel(PLUGIN, player,
                "ListWarps",
                player.getName(),
                player.hasPermission("masuitewarps.list.global"),
                player.hasPermission("masuitewarps.list.server"),
                player.hasPermission("masuitewarps.list.hidden")).send();
        return true;
    }

    // TODO
    private boolean delwarpCmd(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String warpName = args[0];
            if (!WARP_NAME_PATTERN.matcher(warpName).matches()) {
                PLUGIN.formator.sendMessage(sender, PLUGIN.config.load(null, "messages.yml").getString("Msg.invalid-warp-name"));
                return false;
            }
            new BukkitPluginChannel(PLUGIN, null, "DelWarp", sender.getName(), warpName).send();
            return true;
        }

        if (args.length == 2) {
            if (!sender.hasPermission("masuitehomes.home.delete.other")) {
                return false;
            }

            String warpName = args[1];
            if (!WARP_NAME_PATTERN.matcher(warpName).matches()) {
                PLUGIN.formator.sendMessage(sender, PLUGIN.config.load(null, "messages.yml").getString("Msg.invalid-warp-name"));
                return false;
            }

            String ownerName = args[0];
            new BukkitPluginChannel(PLUGIN, null, "DelWarpOther", sender.getName(), warpName, ownerName).send();
            return true;
        }

        PLUGIN.formator.sendMessage(sender, PLUGIN.config.load(null, "messages.yml").getString("Msg.wrong-command-format"));
        return false;
    }

    private boolean setwarpCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        // TODO: support more args
        if (args.length != 1) {
            PLUGIN.formator.sendMessage(sender, PLUGIN.config.load(null, "messages.yml").getString("Msg.wrong-command-format"));
            return false;
        }

        Player player = (Player) sender;
        String warpName = args[0];
        if (!WARP_NAME_PATTERN.matcher(warpName).matches()) {
            PLUGIN.formator.sendMessage(player, PLUGIN.config.load(null, "messages.yml").getString("Msg.invalid-warp-name"));
            return false;
        }

        String publicity = "public";
        String type = "global";
        Location location = player.getLocation();
        String serializedLocation = BukkitAdapter.adapt(location).serialize();

        new BukkitPluginChannel(PLUGIN, player, "SetWarp", player.getName(), warpName, serializedLocation, "hidden".equalsIgnoreCase(publicity), "global".equalsIgnoreCase(type)).send();
        return true;
    }

    private boolean warpCmd(CommandSender sender, String[] args) {
        if (args.length == 0) {
            PLUGIN.formator.sendMessage(sender, PLUGIN.config.load(null, "messages.yml").getString("Msg.wrong-command-format"));
            return false;
        }

        // Check warp name format
        String warpName = args[0];
        if (!WARP_NAME_PATTERN.matcher(warpName).matches()) {
            PLUGIN.formator.sendMessage(sender, PLUGIN.config.load(null, "messages.yml").getString("Msg.invalid-warp-name"));
            return false;
        }

        Warp warp = PLUGIN.warps.get(warpName);
        if (warp == null) {
            PLUGIN.formator.sendMessage(sender, PLUGIN.config.load(null, "messages.yml").getString("Msg.no-such-warp"));
            return false;
        }

        // Executed by console
        if (!(sender instanceof Player)) {
            if (args.length != 2) {
                return false;
            }

            Player playerToWarp = Bukkit.getPlayer(args[1]);
            if (playerToWarp == null || !playerToWarp.isOnline()) {
                PLUGIN.formator.sendMessage(sender, PLUGIN.config.load(null, "messages.yml").getString("Msg.no-such-player"));
                return false;
            }

            new BukkitPluginChannel(PLUGIN, playerToWarp, "Warp", playerToWarp.getName(), warpName, true, true, true, true).send();
            return true;
        }

        // Executed by player
        Player player = (Player) sender;
        if (warp.isHidden() && !player.hasPermission("masuitewarps.warp.hidden")) {
            PLUGIN.formator.sendMessage(player, PLUGIN.config.load(null, "messages.yml").getString("Msg.no-permission"));
            return false;
        }

        PLUGIN.api.getWarmupService().applyWarmup(player, "masuitewarps.warmup.override", "warps", success -> {
            if (success) {
                new BukkitPluginChannel(PLUGIN, player, "MaSuiteTeleports", "GetLocation", player.getName(), BukkitAdapter.adapt(player.getLocation()).serialize()).send();
                new BukkitPluginChannel(PLUGIN, player, "Warp", player.getName(), warpName.toLowerCase(),
                        player.hasPermission("masuitewarps.warp.global"),
                        player.hasPermission("masuitewarps.warp.server"),
                        player.hasPermission("masuitewarps.warp.hidden"),
                        true).send();
            }
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
