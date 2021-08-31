package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TeleportController {
    private final MaSuiteWarps plugin;

    public TeleportController(MaSuiteWarps p) {
        plugin = p;
    }

    public void teleport(ProxiedPlayer player, String name, boolean hasAccessToGlobal, boolean hasAccessToServer, boolean hasAccessToHidden, boolean silent) {
        Warp warp = plugin.getWarpService().getWarp(name);
        if (checkWarp(player, warp, hasAccessToGlobal, hasAccessToServer, hasAccessToHidden)) {
            plugin.getWarpService().teleportToWarp(player, warp, silent);
        }
    }

    private boolean checkWarp(ProxiedPlayer player, Warp warp, boolean hasAccessToGlobal, boolean hasAccessToServer, boolean hasAccessToHidden) {
        if (player == null) {
            return false;
        }
        if (warp == null) {
            plugin.formator.sendMessage(player, plugin.warpNotFound);
            return false;
        }
        if (warp.isHidden() && !hasAccessToHidden) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (warp.isGlobal() && !hasAccessToGlobal) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (!warp.isGlobal() && !hasAccessToServer) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (!warp.isGlobal()) {
            if (!player.getServer().getInfo().getName().equals(warp.getLocation().getServer())) {
                plugin.formator.sendMessage(player, plugin.warpInOtherServer);
                return false;
            }
        }
        return true;
    }
}
