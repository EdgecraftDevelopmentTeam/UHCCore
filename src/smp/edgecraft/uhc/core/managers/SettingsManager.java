package smp.edgecraft.uhc.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import smp.edgecraft.uhc.core.UHCCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Handles the plugin settings
 */
public class SettingsManager {

    /**
     * A list of all the setting managers
     */
    private static final HashMap<String, SettingsManager> configs = new HashMap<>();
    /**
     * The config file
     */
    private File file;
    /**
     * The config file instance (has some getter methods)
     */
    private FileConfiguration config;

    /**
     * Create a new settings / config manager
     *
     * @param fileName The path to the config file
     */
    protected SettingsManager(String fileName) {
        if (!UHCCore.instance.getDataFolder().exists()) {
            try {
                UHCCore.instance.getDataFolder().mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.file = new File(UHCCore.instance.getDataFolder().getAbsolutePath() + "/" + fileName + ".yml");

        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
                InputStream input = UHCCore.instance.getResource("uhc.yml");
                FileOutputStream output = new FileOutputStream(this.file.getAbsolutePath());
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }
                // Load in default uhc.yml
                input.close();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * Gets the config if present, otherwise we will create the config
     *
     * @param name The name of the config file
     * @return the requested config
     */
    public static SettingsManager getConfig(String name) {
        if (configs.containsKey(name)) {
            return configs.get(name);
        }
        SettingsManager manager = new SettingsManager(name);
        configs.put(name, manager);
        return manager;
    }

    /**
     * Gets the data at the given path
     *
     * @param path the path to the data
     * @param <T>  the type in which the data object is
     * @return the data at the given path
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        return (T) this.config.get(path);
    }

    /**
     * Get a location from the config
     *
     * @param path  The path to the location data in the config
     * @param world The world the location is supposed to point to
     * @return the location from the config at the given path
     */
    public Location getLocation(String path, World world) {
        return new Location(world, this.<Integer>get(path + ".x"),
                this.<Integer>get(path + ".y"), this.<Integer>get(path + ".z"),
                this.contains(path + ".rx") ? this.<Double>get(path + ".rx").floatValue() : 0.0F,
                this.contains(path + ".rz") ? this.<Double>get(path + ".rz").floatValue() : 0.0F);
    }

    /**
     * Get a location from the config. Assumes the world name is saved in the config
     *
     * @param path The path to the location data in the config
     * @return the location from the config at the given path
     */
    public Location getLocation(String path) {
        return getLocation(path, Bukkit.getWorld(this.<String>get(path + ".world")));
    }

    /**
     * Set a value in the config at the given path
     *
     * @param path  The path to the value
     * @param value The vale to set
     * @return The updated config
     */
    public SettingsManager set(String path, Object value) {
        this.config.set(path, value);
        save();
        return this;
    }

    /**
     * Checks whether there is data at the given path
     *
     * @param path the path to check
     * @return Whether there is data at the given path
     */
    public boolean contains(String path) {
        return this.config.contains(path);
    }

    /**
     * @return the base config object
     */
    public FileConfiguration getConfig() {
        return this.config;
    }

    /**
     * Save the config
     */
    private void save() {
        try {
            this.config.save(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
