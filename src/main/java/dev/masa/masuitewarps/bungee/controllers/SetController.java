package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;
import java.util.UUID;

/**
 * @author Masa
 */
public class SetController {

    private final MaSuiteWarps plugin;

    public SetController(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void setWarp(ProxiedPlayer player, String name, Location loc, boolean publicity, boolean type) {
        Warp warp = plugin.getWarpService().getWarp(name);

        loc.setServer(player.getServer().getInfo().getName());
        UUID owner = player.getUniqueId();

        if (warp != null) {
            warp.setHidden(publicity);
            warp.setGlobal(type);
            warp.setLocation(loc);
            warp.setOwner(owner);
            plugin.getWarpService().updateWarp(warp);
        } else {
            warp = new Warp(name, publicity, type, loc, owner);
            plugin.getWarpService().createWarp(warp);
        }
        plugin.formator.sendMessage(player, plugin.warpUpdated.replace("%warp%", warp.getName()));

        for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
            ServerInfo serverInfo = entry.getValue();
            Warp finalWarp = warp;
            serverInfo.ping((result, error) -> {
                if (error == null) {
                    new BungeePluginChannel(plugin, serverInfo, "CreateWarp", finalWarp.serialize()).send();
                }
            });
        }
    }
}
