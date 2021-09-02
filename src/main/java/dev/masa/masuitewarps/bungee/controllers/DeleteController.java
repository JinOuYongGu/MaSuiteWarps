package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.bungee.chat.Formator;
import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitecore.core.configuration.BungeeConfiguration;
import dev.masa.masuitecore.core.models.MaSuitePlayer;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;

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

    public void deleteWarp(ProxiedPlayer player, String warpName) {
        Warp warp = plugin.getWarpService().getWarp(warpName);
        if (warp == null) {
            plugin.formator.sendMessage(player, plugin.warpNotFound);
            return;
        }
        if (plugin.getWarpService().removeWarp(warp)) {
            plugin.formator.sendMessage(player, plugin.warpDeleted.replace("%warp%", warp.getName()));
            for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
                ServerInfo serverInfo = entry.getValue();
                serverInfo.ping((result, error) -> {
                    if (error == null) {
                        new BungeePluginChannel(plugin, serverInfo, "DelWarp", warpName).send();
                    }
                });
            }
        } else {
            plugin.formator.sendMessage(player, "&cAn error occurred. Please check console for more details");
        }
    }

    public void deleteOthersWarp(ProxiedPlayer player, String warpName, String ownerName) {
        MaSuitePlayer mspOwner = plugin.getApi().getPlayerService().getPlayer(ownerName);
        if (mspOwner == null) {
            formator.sendMessage(player, config.load("warps", "messages.yml").getString("player-not-found"));
            return;
        }
        deleteWarp(player, warpName);
    }
}
