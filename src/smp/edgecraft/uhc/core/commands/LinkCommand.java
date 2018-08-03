package smp.edgecraft.uhc.core.commands;

import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.discord.UHCBot;

@CommandInfo(aliases = { "link" }, description = "Link your Minecraft account with discord")
public class LinkCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        UHCBot.link(player);
    }
}
