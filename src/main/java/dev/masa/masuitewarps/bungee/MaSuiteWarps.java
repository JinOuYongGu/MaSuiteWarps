package dev.masa.masuitewarps.bungee;

import dev.masa.masuitecore.bungee.Utils;
import dev.masa.masuitecore.bungee.chat.Formator;
import dev.masa.masuitecore.core.Updator;
import dev.masa.masuitecore.core.api.MaSuiteCoreAPI;
import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitecore.core.configuration.BungeeConfiguration;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.bungee.controllers.DeleteController;
import dev.masa.masuitewarps.bungee.controllers.ListController;
import dev.masa.masuitewarps.bungee.controllers.SetController;
import dev.masa.masuitewarps.bungee.controllers.TeleportController;
import dev.masa.masuitewarps.core.services.WarpService;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Masa
 */
public class MaSuiteWarps extends Plugin implements Listener {
    private static MaSuiteWarps plugin;
    private static TeleportController teleportController;
    private static SetController setController;
    private static DeleteController deleteController;
    private static ListController listController;
    @Getter
    private final MaSuiteCoreAPI api = new MaSuiteCoreAPI();
    public Utils utils = new Utils();
    public BungeeConfiguration config = new BungeeConfiguration();
    public Formator formator = new Formator();
    public boolean perWarpPermission = false;
    @Getter
    private Configuration messageConfig = null;
    @Getter
    private WarpService warpService;

    public static MaSuiteWarps getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Config and message
        config.create(this, "warps", "messages.yml");
        config.create(this, "warps", "settings.yml");
        messageConfig = config.load("warps", "messages.yml");

        // Controllers
        teleportController = new TeleportController();
        setController = new SetController();
        deleteController = new DeleteController();
        listController = new ListController();

        getProxy().getPluginManager().registerListener(this, this);

        warpService = new WarpService(this);
        warpService.initializeWarps();

        // Send list of warp
        this.warpService.sendAllWarpsToServers();

        // Updator
        new Updator(getDescription().getVersion(), getDescription().getName(), "60454").checkUpdates();
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) throws IOException {
        if (!"BungeeCord".equals(e.getTag())) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        String subchannel = in.readUTF();

        if ("ListWarps".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            listController.listWarp(player);
        }

        if ("Warp".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            teleportController.teleport(player, in.readUTF());
        }

        if ("CheckPerWarpFlag".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            new BungeePluginChannel(this, player.getServer().getInfo(), "SetPerWarpFlag", perWarpPermission).send();
        }

        if ("SetWarp".equals(subchannel)) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (player == null) {
                return;
            }
            String name = in.readUTF();
            Location location = new Location().deserialize(in.readUTF());
            int maxWarpCount = in.readInt();

            setController.setWarp(player, name, location, maxWarpCount);
        }
        if ("DelWarp".equals(subchannel)) {
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            deleteController.deleteWarp(p, p.getUniqueId(), in.readUTF());
        }
        if ("DelWarpOther".equalsIgnoreCase(subchannel)) {
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            deleteController.deleteOthersWarp(p, in.readUTF(), in.readUTF());
        }
        if ("RequestWarps".equals(subchannel)) {
            this.getWarpService().sendAllWarpsToServers();
        }
    }
}
