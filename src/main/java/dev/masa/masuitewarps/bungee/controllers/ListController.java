package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

/**
 * @author Masa
 */
public class ListController {

    private final MaSuiteWarps plugin;

    public ListController(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void listWarp(ProxiedPlayer player) {
        TextComponent listText = new TextComponent(plugin.formator.colorize(plugin.listHeader));

        final List<Warp> warps = plugin.getWarpService().getPlayerWarps(player.getUniqueId());

        int i = 0;
        String split = plugin.formator.colorize(plugin.listWarpSplitter);
        for (Warp warp : warps) {
            listText.addExtra(buildAndAddListElement(warp));
            if (i != warps.size() - 1) {
                listText.addExtra(split);
            }
            i++;
        }

        player.sendMessage(listText);
    }

    private TextComponent buildAndAddListElement(Warp warp) {
        TextComponent textComponent = new TextComponent(plugin.formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));

        return textComponent;
    }
}
