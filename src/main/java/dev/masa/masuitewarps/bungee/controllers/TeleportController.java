package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Masa
 * @author Jin_ou
 */
public class TeleportController {
    private final MaSuiteWarps plugin;

    public TeleportController(MaSuiteWarps p) {
        plugin = p;
    }

    public void teleport(@NotNull ProxiedPlayer player, @NotNull String name) {
        Warp warp = plugin.getWarpService().getWarp(name);
        if (warp == null) {
            plugin.formator.sendMessage(player, plugin.warpNotFound);
            return;
        }

        plugin.getWarpService().teleportToWarp(player, warp);
    }
}
