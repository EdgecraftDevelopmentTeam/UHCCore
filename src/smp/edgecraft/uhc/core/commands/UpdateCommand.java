package smp.edgecraft.uhc.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.UHCCore;
import smp.edgecraft.uhc.core.managers.UHCManager;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@CommandInfo(aliases = { "update" }, description = "Update the plugin")
public class UpdateCommand extends GameCommand {

    public static final String DOWNLOAD_URL = "https://github.com/EdgecraftDevelopmentTeam/UHCCore/blob/master/out/artifacts/UHCCore/UHCCore.jar?raw=true";

    @Override
    public void onCommand(Player player, String[] args) {
        try {
            URL url = new URL(DOWNLOAD_URL);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(UHCCore.instance.getDataFolder().getParent() + "UHCCore.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            Bukkit.reload();
            UHCManager.announce(ChatColor.GREEN + "Updated plugin!");
        } catch (Exception e) {
            UHCManager.announce(ChatColor.RED + e.toString());
            e.printStackTrace();
        }
    }
}
