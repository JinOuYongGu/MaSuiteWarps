package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.core.configuration.BungeeConfiguration;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.UUID;

/**
 * @author Masa
 */
public class SetController {

    private final MaSuiteWarps plugin;
    private final BungeeConfiguration config = new BungeeConfiguration();

    public SetController(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    /**
     * @param player       command sender
     * @param warpName     name of the warp
     * @param loc          location of the warp
     * @param maxWarpCount the max count of the player's warps, which is unlimited if equals -1
     */
    public void setWarp(ProxiedPlayer player, String warpName, Location loc, int maxWarpCount) {
        List<Warp> playerWarps = plugin.getWarpService().getPlayerWarps(player.getUniqueId());
        if (maxWarpCount >= 0 && playerWarps.size() >= maxWarpCount) {
            plugin.formator.sendMessage(player, config.load("warps", "messages.yml").getString("warp-limit-reached"));
            return;
        }

        // Update the location's server to current server
        loc.setServer(player.getServer().getInfo().getName());
        UUID owner = player.getUniqueId();

        Warp warp = plugin.getWarpService().getWarp(warpName);
        if (warp != null) {
            if (!warp.getOwner().equals(owner)) {
                plugin.formator.sendMessage(player, config.load("warps", "messages.yml").getString("not-owner"));
                return;
            }

            warp.setLocation(loc);
            warp.setOwner(owner);
            plugin.getWarpService().updateWarp(warp);

            plugin.formator.sendMessage(player, plugin.warpUpdated.replace("%warp%", warp.getName()));
            return;
        } else {
            warp = new Warp(warpName, loc, owner);
            plugin.getWarpService().createWarp(warp);

            plugin.formator.sendMessage(player, plugin.warpCreated.replace("%warp%", warp.getName()));
            return;
        }
    }
}
