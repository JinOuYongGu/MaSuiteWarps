package dev.masa.masuitewarps.bukkit;

import dev.masa.masuitecore.core.adapters.BukkitAdapter;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.core.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Masa
 */
public class WarpMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private final MaSuiteWarps plugin;

    public WarpMessageListener(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!"BungeeCord".equals(channel)) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        String subchannel;
        try {
            subchannel = in.readUTF();
            if ("WarpPlayer".equals(subchannel)) {
                Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
                if (p == null) {
                    return;
                }
                Location loc = new Location().deserialize(in.readUTF());

                org.bukkit.Location bukkitLocation = BukkitAdapter.adapt(loc);
                if (bukkitLocation.getWorld() == null) {
                    System.out.println("[MaSuite] [Warps] [World=" + loc.getWorld() + "] World  could not be found!");
                    return;
                }
                p.teleport(bukkitLocation);
            }
            if ("CreateWarp".equals(subchannel)) {
                Warp warp = new Warp();
                warp = warp.deserialize(in.readUTF().toLowerCase());
                plugin.warps.put(warp.getName(), warp);
            }
            if ("WarpCooldown".equals(subchannel)) {
                Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
                if (p == null) {
                    return;
                }
            }
            if ("SetPerWarpFlag".equals(subchannel)) {
                plugin.perServerWarps = in.readBoolean();
            }
            if ("DelWarp".equals(subchannel)) {
                plugin.warps.remove(in.readUTF());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
