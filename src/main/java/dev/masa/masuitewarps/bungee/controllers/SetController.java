package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * @author Masa
 */
public class SetController {
    private final MaSuiteWarps plugin = MaSuiteWarps.getPlugin();
    private final Configuration message = plugin.getMessageConfig();

    /**
     * @param player       command sender
     * @param warpName     name of the warp
     * @param loc          location of the warp
     * @param maxWarpCount the max count of the player's warps, which is unlimited if equals -1
     */
    public void setWarp(@NotNull ProxiedPlayer player, @NotNull String warpName, @NotNull Location loc, int maxWarpCount) {
        List<Warp> playerWarps = plugin.getWarpService().getPlayerWarps(player.getUniqueId());
        if (maxWarpCount >= 0 && playerWarps.size() >= maxWarpCount) {
            plugin.formator.sendMessage(player, message.getString("warp-limit-reached"));
            return;
        }

        // Update the location's server to current server
        loc.setServer(player.getServer().getInfo().getName());
        UUID owner = player.getUniqueId();

        Warp warp = plugin.getWarpService().getWarp(warpName);
        if (warp != null) {
            if (!warp.getOwner().equals(owner)) {
                plugin.formator.sendMessage(player, message.getString("not-owner"));
                return;
            }

            warp.setLocation(loc);
            warp.setOwner(owner);
            plugin.getWarpService().updateWarp(warp);

            plugin.formator.sendMessage(player, message.getString("warp-updated").replace("%warp%", warp.getName()));
        } else {
            warp = new Warp(warpName, loc, owner);
            plugin.getWarpService().createWarp(warp);

            plugin.formator.sendMessage(player, message.getString("warp-created").replace("%warp%", warp.getName()));
        }
    }
}
