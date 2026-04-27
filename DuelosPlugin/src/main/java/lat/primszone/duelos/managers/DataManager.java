package lat.primszone.duelos.managers;

import lat.primszone.duelos.DuelosPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final DuelosPlugin plugin;
    private File dataFile;
    private FileConfiguration data;
    // Mapa UUID → nombre
    private final java.util.Map<UUID, String> nombres = new java.util.HashMap<>();

    public DataManager(DuelosPlugin plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void load() {
        data = YamlConfiguration.loadConfiguration(dataFile);
        EloManager elo = plugin.getEloManager();
        if (!data.contains("players")) return;
        for (String uuidStr : data.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            String path = "players." + uuidStr;
            elo.setElo(uuid,    data.getInt(path + ".elo", 1000));
            elo.setWins(uuid,   data.getInt(path + ".wins", 0));
            elo.setLosses(uuid, data.getInt(path + ".losses", 0));
            elo.setKills(uuid,  data.getInt(path + ".kills", 0));
            elo.setDeaths(uuid, data.getInt(path + ".deaths", 0));
            elo.setStreak(uuid, data.getInt(path + ".streak", 0));
            elo.setKeepin(uuid, data.getBoolean(path + ".keepinv", false));
            elo.setSkillsOff(uuid, data.getBoolean(path + ".skillsoff", false));
            nombres.put(uuid,   data.getString(path + ".name", "?"));
            elo.getNombres().put(uuid, data.getString(path + ".name", "?"));
        }
    }

    public void save() {
        EloManager elo = plugin.getEloManager();
        for (Map.Entry<UUID, Integer> entry : elo.getEloMap().entrySet()) {
            UUID uuid = entry.getKey();
            String path = "players." + uuid;
            data.set(path + ".elo",    elo.getElo(uuid));
            data.set(path + ".wins",   elo.getWins(uuid));
            data.set(path + ".losses", elo.getLosses(uuid));
            data.set(path + ".kills",  elo.getKills(uuid));
            data.set(path + ".deaths", elo.getDeaths(uuid));
            data.set(path + ".streak", elo.getStreak(uuid));
            data.set(path + ".keepinv",elo.isKeepin(uuid));
            data.set(path + ".skillsoff",elo.isSkillsOff(uuid));
            String nom = elo.getNombres().get(uuid);
            if (nom != null) data.set(path + ".name", nom);
        }
        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
