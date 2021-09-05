package dev.masa.masuitewarps.bukkit;

import dev.masa.masuitecore.bukkit.chat.Formator;
import dev.masa.masuitecore.core.Updator;
import dev.masa.masuitecore.core.api.MaSuiteCoreBukkitAPI;
import dev.masa.masuitecore.core.channels.BukkitPluginChannel;
import dev.masa.masuitecore.core.configuration.BukkitConfiguration;
import dev.masa.masuitewarps.bukkit.commands.MaSuiteWarpsCommand;
import dev.masa.masuitewarps.core.models.Warp;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author Masa
 */
public class MaSuiteWarps extends JavaPlugin implements Listener {
    @Getter
    private static MaSuiteWarps plugin;
    public MaSuiteCoreBukkitAPI api = new MaSuiteCoreBukkitAPI();
    public HashMap<String, Warp> warps = new HashMap<>();
    public BukkitConfiguration config = new BukkitConfiguration();
    public Formator formator = new Formator();
    public boolean perServerWarps = false;
    private boolean requestedPerServerWarps = false;

    @Override
    public void onEnable() {
        plugin = this;

        // Create config and message file
        config.create(this, "warps", "config.yml");
        config.create(this, "warps", "messages.yml");

        // Register listeners
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new WarpMessageListener(this));

        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        //getServer().getPluginManager().registerEvents(new Sign(this), this);

        // Register commands
        MaSuiteWarpsCommand maSuiteWarpsCommand = new MaSuiteWarpsCommand();
        for (final String commandName : getDescription().getCommands().keySet()) {
            Objects.requireNonNull(Bukkit.getPluginCommand(commandName)).setTabCompleter(maSuiteWarpsCommand);
            Objects.requireNonNull(Bukkit.getPluginCommand(commandName)).setExecutor(maSuiteWarpsCommand);
        }

        new Updator(getDescription().getVersion(), getDescription().getName(), "60454").checkUpdates();

        api.getCooldownService().addCooldownLength("warps", config.load("warps", "config.yml").getInt("cooldown"));
        api.getWarmupService().addWarmupTime("warps", config.load("warps", "config.yml").getInt("warmup"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (warps.isEmpty()) {
            getServer().getScheduler().runTaskLaterAsynchronously(this, () -> new BukkitPluginChannel(this, e.getPlayer(), "RequestWarps").send(), 100);
        }
        if (!requestedPerServerWarps) {
            getServer().getScheduler().runTaskLaterAsynchronously(this, () -> new BukkitPluginChannel(this, e.getPlayer(), "CheckPerWarpFlag", e.getPlayer().getName()).send(), 100);
            requestedPerServerWarps = true;
        }
    }

}
