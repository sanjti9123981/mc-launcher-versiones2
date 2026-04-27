package lat.primszone.duelos.managers;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Duelo;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class QueueManager {

    private final DuelosPlugin plugin;

    // Modo → lista de jugadores en cola (FIFO)
    private final Map<Duelo.Modo, List<UUID>> colas = new EnumMap<>(Duelo.Modo.class);
    // UUID → solicitudes de reto pendientes: UUID_retador → modo
    private final Map<UUID, Map<UUID, Duelo.Modo>> retos = new HashMap<>();

    public QueueManager(DuelosPlugin plugin) {
        this.plugin = plugin;
        for (Duelo.Modo modo : Duelo.Modo.values()) {
            colas.put(modo, new LinkedList<>());
        }
        iniciarLoop();
    }

    // ─── Cola automática ──────────────────────────────────────────────────
    public boolean entrarCola(Player p, Duelo.Modo modo) {
        if (estaEnCola(p.getUniqueId())) return false;
        if (plugin.getDuelManager().estaEnDuelo(p.getUniqueId())) return false;
        colas.get(modo).add(p.getUniqueId());
        return true;
    }

    public boolean salirCola(UUID uuid) {
        for (List<UUID> cola : colas.values()) {
            if (cola.remove(uuid)) return true;
        }
        return false;
    }

    public boolean estaEnCola(UUID uuid) {
        for (List<UUID> cola : colas.values()) {
            if (cola.contains(uuid)) return true;
        }
        return false;
    }

    public Duelo.Modo getModoEnCola(UUID uuid) {
        for (Map.Entry<Duelo.Modo, List<UUID>> entry : colas.entrySet()) {
            if (entry.getValue().contains(uuid)) return entry.getKey();
        }
        return null;
    }

    public int getTamanoCola(Duelo.Modo modo) {
        return colas.get(modo).size();
    }

    private void iniciarLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Duelo.Modo modo : Duelo.Modo.values()) {
                    List<UUID> cola = colas.get(modo);
                    // Limpiar offline / en duelo
                    cola.removeIf(uuid -> {
                        Player p = plugin.getServer().getPlayer(uuid);
                        return p == null || !p.isOnline() || plugin.getDuelManager().estaEnDuelo(uuid);
                    });
                    if (cola.size() >= 2) {
                        UUID u1 = cola.remove(0);
                        UUID u2 = cola.remove(0);
                        Player p1 = plugin.getServer().getPlayer(u1);
                        Player p2 = plugin.getServer().getPlayer(u2);
                        if (p1 != null && p2 != null) {
                            plugin.getDuelManager().iniciarDuelo(p1, p2, modo);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 60L); // cada 3 segundos
    }

    // ─── Retos directos ──────────────────────────────────────────────────
    public void enviarReto(Player retador, Player target, Duelo.Modo modo) {
        retos.computeIfAbsent(target.getUniqueId(), k -> new HashMap<>())
             .put(retador.getUniqueId(), modo);

        // Auto-expirar en 30 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<UUID, Duelo.Modo> pending = retos.get(target.getUniqueId());
                if (pending != null && pending.containsKey(retador.getUniqueId())) {
                    pending.remove(retador.getUniqueId());
                    if (pending.isEmpty()) retos.remove(target.getUniqueId());
                    if (retador.isOnline())
                        retador.sendMessage("§8[§c!§8] §cTu reto a §f" + target.getName() + "§c expiró.");
                    if (target.isOnline())
                        target.sendMessage("§8[§c!§8] §cEl reto de §f" + retador.getName() + "§c expiró.");
                }
            }
        }.runTaskLater(plugin, 600L); // 30 segundos
    }

    public boolean tieneRetoPendiente(UUID targetUUID, UUID retadorUUID) {
        Map<UUID, Duelo.Modo> pending = retos.get(targetUUID);
        return pending != null && pending.containsKey(retadorUUID);
    }

    public Map<UUID, Duelo.Modo> getRetosPendientes(UUID targetUUID) {
        return retos.getOrDefault(targetUUID, Collections.emptyMap());
    }

    public Duelo.Modo aceptarReto(UUID targetUUID, UUID retadorUUID) {
        Map<UUID, Duelo.Modo> pending = retos.get(targetUUID);
        if (pending == null) return null;
        Duelo.Modo modo = pending.remove(retadorUUID);
        if (pending.isEmpty()) retos.remove(targetUUID);
        return modo;
    }

    public void limpiarRetos(UUID uuid) {
        retos.remove(uuid);
        for (Map<UUID, Duelo.Modo> m : retos.values()) m.remove(uuid);
    }
}
