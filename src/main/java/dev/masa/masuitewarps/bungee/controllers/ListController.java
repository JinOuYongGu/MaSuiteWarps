package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Masa
 */
public class ListController {

    private final MaSuiteWarps plugin = MaSuiteWarps.getPlugin();
    private final Configuration message = plugin.getMessageConfig();

    public void listWarp(@NotNull ProxiedPlayer player) {
        TextComponent listText = new TextComponent(plugin.formator.colorize(message.getString("warp.list")));

        final List<Warp> warps = plugin.getWarpService().getPlayerWarps(player.getUniqueId());

        int i = 0;
        String split = plugin.formator.colorize(message.getString("warp.split"));
        for (Warp warp : warps) {
            listText.addExtra(buildAndAddListElement(warp));
            if (i != warps.size() - 1) {
                listText.addExtra(split);
            }
            i++;
        }

        player.sendMessage(listText);
    }

    private @NotNull TextComponent buildAndAddListElement(@NotNull Warp warp) {
        TextComponent textComponent = new TextComponent(plugin.formator.colorize(message.getString("warp.name").replace("%warp%", warp.getName())));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
        Text hoverText = new Text(plugin.formator.colorize(message.getString("warp-hover-text").replace("%warp%", warp.getName())));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

        return textComponent;
    }
}
