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
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Masa
 */
public class MaSuiteWarps extends Plugin implements Listener {

    private final TeleportController teleportController = new TeleportController(this);
    private final SetController set = new SetController(this);
    private final DeleteController delete = new DeleteController(this);
    private final ListController list = new ListController(this);
    @Getter
    private final MaSuiteCoreAPI api = new MaSuiteCoreAPI();
    public Utils utils = new Utils();
    public BungeeConfiguration config = new BungeeConfiguration();
    public Formator formator = new Formator();
    public boolean perWarpPermission = false;
    public String warpNotFound = "";
    public String noPermission = "";
    public String warpInOtherServer = "";
    public String teleported = "";
    public String listHeaderGlobal = "";
    public String listHeaderServer = "";
    public String listHeaderHidden = "";
    public String listWarpName = "";
    public String listHoverText = "";
    public String listWarpSplitter = "";
    public String warpCreated = "";
    public String warpUpdated = "";
    public String warpDeleted = "";
    @Getter
    private WarpService warpService;

    @Override
    public void onEnable() {
        // Configuration
        config.create(this, "warps", "messages.yml");
        config.create(this, "warps", "settings.yml");
        getProxy().getPluginManager().registerListener(this, this);

        warpService = new WarpService(this);


        warpService.initializeWarps();

        // Send list of warp
        this.warpService.sendAllWarpsToServers();

        // Updator
        new Updator(getDescription().getVersion(), getDescription().getName(), "60454").checkUpdates();

        perWarpPermission = config.load("warps", "settings.yml").getBoolean("enable-per-warp-permission");
        warpNotFound = config.load("warps", "messages.yml").getString("warp-not-found");
        noPermission = config.load("warps", "messages.yml").getString("no-permission");
        warpInOtherServer = config.load("warps", "messages.yml").getString("warp-in-other-server");
        teleported = config.load("warps", "messages.yml").getString("teleported");

        listHeaderGlobal = config.load("warps", "messages.yml").getString("warp.global");
        listHeaderServer = config.load("warps", "messages.yml").getString("warp.server");
        listHeaderHidden = config.load("warps", "messages.yml").getString("warp.hidden");

        listWarpName = config.load("warps", "messages.yml").getString("warp.name");
        listHoverText = config.load("warps", "messages.yml").getString("warp-hover-text");
        listWarpSplitter = config.load("warps", "messages.yml").getString("warp.split");

        warpCreated = config.load("warps", "messages.yml").getString("warp-created");
        warpUpdated = config.load("warps", "messages.yml").getString("warp-updated");
        warpDeleted = config.load("warps", "messages.yml").getString("warp-deleted");
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
            if (player == null) {
                return;
            }
            list.listWarp(player, in.readBoolean(), in.readBoolean(), in.readBoolean());
        }

        if ("Warp".equals(subchannel)) {
            teleportController.teleport(getProxy().getPlayer(in.readUTF()), in.readUTF(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean());
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

            set.setWarp(player, name, location, in.readBoolean(), in.readBoolean());
        }
        if ("DelWarp".equals(subchannel)) {
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            delete.deleteWarp(p, in.readUTF());
        }
        if ("RequestWarps".equals(subchannel)) {
            this.getWarpService().sendAllWarpsToServers();
        }
    }
}
