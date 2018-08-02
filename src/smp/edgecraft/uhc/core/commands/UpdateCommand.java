package smp.edgecraft.uhc.core.commands;

import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.UHCCore;

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@CommandInfo(aliases = { "update" }, description = "Update the plugin")
public class UpdateCommand extends GameCommand {

    public static final String DOWNLOAD_URL = "https://github.com/EdgecraftDevelopmentTeam/UHCCore/raw/master/out/artifacts/UHCCore/UHCCore.jar";

    @Override
    public void onCommand(Player player, String[] args) {
        try {
            URL url = new URL(DOWNLOAD_URL);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(UHCCore.instance.getDataFolder().getParent() + "UHCCore.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
