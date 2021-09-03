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
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jin_ou
 */
public class MaSuiteWarpsCommand implements TabCompleter, CommandExecutor {
    private static final MaSuiteWarps PLUGIN = MaSuiteWarps.getPlugin();
    private static final Pattern WARP_NAME_PATTERN = Pattern.compile("^[_a-zA-Z0-9\\u4e00-\\u9fa5]{5,16}$");

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

        if (args.length == 0) {
            new BukkitPluginChannel(PLUGIN, player, "ListWarps", player.getName()).send();
            return true;
        }

        PLUGIN.formator.sendMessage(player, PLUGIN.config.load("warps", "messages.yml").getString("Msg.wrong-command-format"));
        return false;
    }

    private boolean delwarpCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            String warpName = args[0];
            if (!WARP_NAME_PATTERN.matcher(warpName).matches()) {
                PLUGIN.formator.sendMessage(player, PLUGIN.config.load("warps", "messages.yml").getString("Msg.invalid-warp-name"));
                return false;
            }
            new BukkitPluginChannel(PLUGIN, player, "DelWarp", player.getName(), warpName).send();
            return true;
        }

        if (args.length == 2) {
            if (!player.hasPermission("masuitehomes.home.delete.other")) {
                return false;
            }

            String warpName = args[1];
            if (!WARP_NAME_PATTERN.matcher(warpName).matches()) {
                PLUGIN.formator.sendMessage(player, PLUGIN.config.load("warps", "messages.yml").getString("Msg.invalid-warp-name"));
                return false;
            }

            String ownerName = args[0];
            new BukkitPluginChannel(PLUGIN, player, "DelWarpOther", player.getName(), ownerName, warpName).send();
            return true;
        }

        PLUGIN.formator.sendMessage(player, PLUGIN.config.load("warps", "messages.yml").getString("Msg.wrong-command-format"));
        return false;
    }

    private boolean setwarpCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            String warpName = args[0];
            if (!WARP_NAME_PATTERN.matcher(warpName).matches()) {
                PLUGIN.formator.sendMessage(player, PLUGIN.config.load("warps", "messages.yml").getString("Msg.invalid-warp-name"));
                return false;
            }

            Location location = player.getLocation();
            String serializedLocation = BukkitAdapter.adapt(location).serialize();

            new BukkitPluginChannel(PLUGIN, player, "SetWarp", player.getName(), warpName, serializedLocation, getMaxWarpCount(player)).send();
            return true;
        }

        PLUGIN.formator.sendMessage(sender, PLUGIN.config.load("warps", "messages.yml").getString("Msg.wrong-command-format"));
        return false;
    }

    private boolean warpCmd(CommandSender sender, String[] args) {
        if (args.length <= 0) {
            PLUGIN.formator.sendMessage(sender, PLUGIN.config.load("warps", "messages.yml").getString("Msg.wrong-command-format"));
            return false;
        }

        String warpName = args[0];

        // Executed by player
        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;

            PLUGIN.api.getWarmupService().applyWarmup(player, "masuitewarps.warmup.override", "warps", success -> {
                if (success) {
                    new BukkitPluginChannel(PLUGIN, player, "MaSuiteTeleports", "GetLocation", player.getName(), BukkitAdapter.adapt(player.getLocation()).serialize()).send();
                    new BukkitPluginChannel(PLUGIN, player, "Warp", player.getName(), warpName).send();
                }
            });
            return true;
        }

        if (args.length == 2 && !(sender instanceof Player)) {
            // Executed by console
            Player playerToWarp = Bukkit.getPlayer(args[1]);
            if (playerToWarp == null || !playerToWarp.isOnline()) {
                PLUGIN.formator.sendMessage(sender, PLUGIN.config.load("warps", "messages.yml").getString("Msg.no-such-player"));
                return false;
            }
            new BukkitPluginChannel(PLUGIN, playerToWarp, "Warp", playerToWarp.getName(), warpName).send();
            return true;
        }

        PLUGIN.formator.sendMessage(sender, PLUGIN.config.load("warps", "messages.yml").getString("Msg.wrong-command-format"));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    private int getMaxWarpCount(Player player) {
        if (player.isOp()) {
            return -1;
        }

        int maxWarpCount = 0;
        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            String permission = permInfo.getPermission();
            if (permission.startsWith("masuitewarps.warp.limit.")) {
                String count = permission.replace("masuitewarps.warp.limit.", "");
                int warpCount = 0;
                try {
                    warpCount = Integer.parseInt(count);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (warpCount > maxWarpCount) {
                    maxWarpCount = warpCount;
                }
            }
        }
        return maxWarpCount;
    }
}
