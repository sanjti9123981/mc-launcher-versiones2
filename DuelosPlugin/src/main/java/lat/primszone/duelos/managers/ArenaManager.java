package lat.primszone.duelos.managers;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Arena;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private final DuelosPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private final Set<String> arenasOcupadas = new HashSet<>();
    private File arenaFile;
    private FileConfiguration arenaCfg;

    public ArenaManager(DuelosPlugin plugin) {
        this.plugin = plugin;
        arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenaFile.exists()) {
            try { arenaFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        cargar();
    }

    public void cargar() {
        arenaCfg = YamlConfiguration.loadConfiguration(arenaFile);
        arenas.clear();
        if (!arenaCfg.contains("arenas")) return;
        for (String nombre : arenaCfg.getConfigurationSection("arenas").getKeys(false)) {
            Arena a = new Arena(nombre);
            if (arenaCfg.contains("arenas." + nombre + ".spawn1")) {
                a.setSpawn1(deserializeLocation(arenaCfg, "arenas." + nombre + ".spawn1"));
            }
            if (arenaCfg.contains("arenas." + nombre + ".spawn2")) {
                a.setSpawn2(deserializeLocation(arenaCfg, "arenas." + nombre + ".spawn2"));
            }
            if (arenaCfg.contains("arenas." + nombre + ".schematic")) {
                a.setSchematic(arenaCfg.getString("arenas." + nombre + ".schematic"));
            }
            arenas.put(nombre.toLowerCase(), a);
        }
    }

    public void guardar() {
        for (Map.Entry<String, Arena> entry : arenas.entrySet()) {
            Arena a = entry.getValue();
            String path = "arenas." + a.getNombre();
            if (a.getSpawn1() != null) serializeLocation(arenaCfg, path + ".spawn1", a.getSpawn1());
            if (a.getSpawn2() != null) serializeLocation(arenaCfg, path + ".spawn2", a.getSpawn2());
            if (a.getSchematic() != null) arenaCfg.set(path + ".schematic", a.getSchematic());
        }
        try { arenaCfg.save(arenaFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void serializeLocation(FileConfiguration cfg, String path, Location loc) {
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX());
        cfg.set(path + ".y", loc.getY());
        cfg.set(path + ".z", loc.getZ());
        cfg.set(path + ".yaw", loc.getYaw());
        cfg.set(path + ".pitch", loc.getPitch());
    }

    private Location deserializeLocation(FileConfiguration cfg, String path) {
        String world = cfg.getString(path + ".world", "world");
        double x = cfg.getDouble(path + ".x");
        double y = cfg.getDouble(path + ".y");
        double z = cfg.getDouble(path + ".z");
        float yaw = (float) cfg.getDouble(path + ".yaw");
        float pitch = (float) cfg.getDouble(path + ".pitch");
        return new Location(plugin.getServer().getWorld(world), x, y, z, yaw, pitch);
    }

    public Arena crearArena(String nombre) {
        Arena a = new Arena(nombre.toLowerCase());
        arenas.put(nombre.toLowerCase(), a);
        return a;
    }

    public boolean borrarArena(String nombre) {
        if (arenas.remove(nombre.toLowerCase()) != null) {
            arenaCfg.set("arenas." + nombre.toLowerCase(), null);
            try { arenaCfg.save(arenaFile); } catch (IOException e) { e.printStackTrace(); }
            return true;
        }
        return false;
    }

    public Arena getArena(String nombre) { return arenas.get(nombre.toLowerCase()); }
    public Collection<Arena> getTodasLasArenas() { return arenas.values(); }

    public Arena getArenaDisponible() {
        for (Arena a : arenas.values()) {
            if (a.estaCompleta() && !arenasOcupadas.contains(a.getNombre())) {
                return a;
            }
        }
        return null;
    }

    public Arena getArenaAleatoria() {
        List<Arena> disponibles = new ArrayList<>();
        for (Arena a : arenas.values()) {
            if (a.estaCompleta() && !arenasOcupadas.contains(a.getNombre())) {
                disponibles.add(a);
            }
        }
        if (disponibles.isEmpty()) return null;
        return disponibles.get(new Random().nextInt(disponibles.size()));
    }

    public void ocuparArena(String nombre)   { arenasOcupadas.add(nombre.toLowerCase()); }
    public void liberarArena(String nombre)  { arenasOcupadas.remove(nombre.toLowerCase()); }
    public boolean isOcupada(String nombre)  { return arenasOcupadas.contains(nombre.toLowerCase()); }
}
