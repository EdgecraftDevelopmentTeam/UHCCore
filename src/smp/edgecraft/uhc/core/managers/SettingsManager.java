package smp.edgecraft.uhc.core.managers;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SettingsManager {

	private static final HashMap<String, SettingsManager> configs = new HashMap<>();
	
	public static SettingsManager getConfig(String name) {
		if (configs.containsKey(name)) {
			return configs.get(name);
		}
		SettingsManager manager = new SettingsManager(name);
		configs.put(name, manager);
		return manager;
	}

	private File file;
	private FileConfiguration config;

	protected SettingsManager(String fileName) {
		if (!Main.getPlugin().getDataFolder().exists()) {
			try {
				Main.getPlugin().getDataFolder().mkdir();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.file = new File(Main.getPlugin().getDataFolder().getAbsolutePath() + "/" + fileName + ".yml");

		if (!this.file.exists()) {
			try {
				this.file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.config = YamlConfiguration.loadConfiguration(this.file);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String path) {
		return (T) this.config.get(path);
	}

	public Location getLocation(String path) {
		return new Location(Bukkit.getWorld(this.<String>get(path + ".world")), this.<Double>get(path + ".x"),
				this.<Double>get(path + ".y"), this.<Double>get(path + ".z"),
				this.contains(path + ".rx") ? this.<Double>get(path + ".rx").floatValue() : 0.0F,
				this.contains(path + ".rz") ? this.<Double>get(path + ".rz").floatValue() : 0.0F);
	}

	public void set(String path, Object value) {
		this.config.set(path, value);
		save();
	}

	public boolean contains(String path) {
		return this.config.contains(path);
	}

	public ConfigurationSection createSection(String path) {
		ConfigurationSection section = this.config.createSection(path);
		save();
		return section;
	}
	
	public FileConfiguration getConfig() {
		return this.config;
	}

	private void save() {
		try {
			this.config.save(this.file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
