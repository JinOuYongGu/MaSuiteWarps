package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.bungee.chat.Formator;
import dev.masa.masuitecore.core.configuration.BungeeConfiguration;
import dev.masa.masuitecore.core.models.MaSuitePlayer;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * @author Masa
 */
public class DeleteController {

    private final MaSuiteWarps plugin;
    private final Formator formator = new Formator();
    private final BungeeConfiguration config = new BungeeConfiguration();

    public DeleteController(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void deleteWarp(ProxiedPlayer sender, UUID ownerId, String warpName) {
        Warp warp = plugin.getWarpService().getWarp(warpName);
        if (warp == null) {
            plugin.formator.sendMessage(sender, plugin.warpNotFound);
            return;
        }

        if (!warp.getOwner().equals(ownerId)) {
            formator.sendMessage(sender, config.load("warps", "messages.yml").getString("not-owner"));
            return;
        }

        if (plugin.getWarpService().removeWarp(warp)) {
            plugin.formator.sendMessage(sender, plugin.warpDeleted.replace("%warp%", warp.getName()));
        } else {
            plugin.formator.sendMessage(sender, "&cAn error occurred. Please check console for more details");
        }
    }

    public void deleteOthersWarp(ProxiedPlayer sender, String ownerName, String warpName) {
        MaSuitePlayer mspOwner = plugin.getApi().getPlayerService().getPlayer(ownerName);
        if (mspOwner == null) {
            formator.sendMessage(sender, config.load("warps", "messages.yml").getString("player-not-found"));
            return;
        }

        deleteWarp(sender, mspOwner.getUniqueId(), warpName);
    }
}
