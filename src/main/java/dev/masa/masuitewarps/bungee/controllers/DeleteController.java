package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.core.models.MaSuitePlayer;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.UUID;

/**
 * @author Masa
 */
public class DeleteController {

    private final MaSuiteWarps plugin = MaSuiteWarps.getPlugin();
    private final Configuration message = plugin.getMessageConfig();

    public void deleteWarp(ProxiedPlayer sender, UUID ownerId, String warpName) {
        Warp warp = plugin.getWarpService().getWarp(warpName);
        if (warp == null) {
            plugin.formator.sendMessage(sender, message.getString("warp-not-found"));
            return;
        }

        if (!warp.getOwner().equals(ownerId)) {
            plugin.formator.sendMessage(sender, message.getString("not-owner"));
            return;
        }

        if (plugin.getWarpService().removeWarp(warp)) {
            plugin.formator.sendMessage(sender, message.getString("warp-deleted").replace("%warp%", warp.getName()));
        } else {
            plugin.formator.sendMessage(sender, "&cAn error occurred. Please check console for more details");
        }
    }

    public void deleteOthersWarp(ProxiedPlayer sender, String ownerName, String warpName) {
        MaSuitePlayer mspOwner = plugin.getApi().getPlayerService().getPlayer(ownerName);
        if (mspOwner == null) {
            plugin.formator.sendMessage(sender, message.getString("player-not-found"));
            return;
        }

        deleteWarp(sender, mspOwner.getUniqueId(), warpName);
    }
}
