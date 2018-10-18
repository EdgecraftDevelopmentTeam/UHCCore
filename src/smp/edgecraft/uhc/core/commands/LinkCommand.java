package smp.edgecraft.uhc.core.commands;

import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.discord.UHCBot;

/**
 * The link command, used to link a discord account to a player
 */
@CommandInfo(aliases = {"link"}, description = "Link your Minecraft account with discord", permission = "uhc.link")
public class LinkCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        UHCBot.link(player);
    }
}
