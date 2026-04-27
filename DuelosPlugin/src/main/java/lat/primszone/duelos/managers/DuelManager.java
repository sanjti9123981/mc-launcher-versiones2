package lat.primszone.duelos.managers;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Arena;
import lat.primszone.duelos.models.Duelo;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelManager {

    private final DuelosPlugin plugin;
    // UUID de cualquier jugador → Duelo activo
    private final Map<UUID, Duelo> duelosActivos = new HashMap<>();

    public DuelManager(DuelosPlugin plugin) {
        this.plugin = plugin;
        iniciarHPBar();
    }

    // ─── Iniciar duelo ────────────────────────────────────────────────────
    public void iniciarDuelo(Player p1, Player p2, Duelo.Modo modo) {
        Arena arena = plugin.getArenaManager().getArenaAleatoria();
        if (arena == null) {
            p1.sendMessage("§8[§c!§8] §cNo hay arenas disponibles en este momento.");
            p2.sendMessage("§8[§c!§8] §cNo hay arenas disponibles en este momento.");
            return;
        }

        plugin.getArenaManager().ocuparArena(arena.getNombre());
        Duelo duelo = new Duelo(p1, p2, arena.getNombre(), modo);
        duelo.setKeepinv1(plugin.getEloManager().isKeepin(p1.getUniqueId()));
        duelo.setKeepinv2(plugin.getEloManager().isKeepin(p2.getUniqueId()));

        duelosActivos.put(p1.getUniqueId(), duelo);
        duelosActivos.put(p2.getUniqueId(), duelo);

        // Mostrar ajustes del duelo
        String modNombre = modo == Duelo.Modo.NOCRISTMAZA ? "§5§lNO CRYSTAL/MACE" : "§c§lNORMAL";
        String keepMsg1 = duelo.isKeepinv1() ? "§aConservar inventario §8(§c-30%% ELO§8)" : "§cSin conservar §8(§ax2 ELO§8)";
        String keepMsg2 = duelo.isKeepinv2() ? "§aConservar inventario §8(§c-30%% ELO§8)" : "§cSin conservar §8(§ax2 ELO§8)";

        for (Player p : new Player[]{p1, p2}) {
            p.sendMessage("");
            p.sendMessage("  §8§m━━━━━━━━━━━━━━━━━━━━━━━");
            p.sendMessage("  §f§lRIVAL ENCONTRADO");
            p.sendMessage("  §8Modo:   " + modNombre);
            p.sendMessage("  §8Arena:  §f" + arena.getNombre());
            p.sendMessage("  §8Rival:  §f" + (p == p1 ? p2.getName() : p1.getName()));
        }
        p1.sendMessage("  §8Inv:    " + keepMsg1);
        p2.sendMessage("  §8Inv:    " + keepMsg2);
        for (Player p : new Player[]{p1, p2}) {
            p.sendMessage("  §8§m━━━━━━━━━━━━━━━━━━━━━━━");
            p.sendMessage("");
        }

        // Countdown 3-2-1
        new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (count > 0) {
                    String title = "§e§l" + count;
                    p1.sendTitle(title, "§7vs §e" + p2.getName(), 2, 16, 2);
                    p2.sendTitle(title, "§7vs §e" + p1.getName(), 2, 16, 2);
                    p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                    p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                    count--;
                } else {
                    // Teleportar a los spawns
                    p1.teleport(arena.getSpawn1());
                    p2.teleport(arena.getSpawn2());
                    p1.sendTitle("§a§l¡PELEA!", "§7Que gane el mejor", 5, 30, 5);
                    p2.sendTitle("§a§l¡PELEA!", "§7Que gane el mejor", 5, 30, 5);
                    p1.playSound(p1.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
                    p2.playSound(p2.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
                    duelo.setEstado(Duelo.Estado.ACTIVO);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // ─── Terminar duelo ───────────────────────────────────────────────────
    public void terminarDuelo(UUID ganadorUUID, UUID perdedorUUID, String motivo) {
        Duelo duelo = duelosActivos.get(ganadorUUID);
        if (duelo == null) duelo = duelosActivos.get(perdedorUUID);
        if (duelo == null) return;

        duelo.setEstado(Duelo.Estado.TERMINADO);
        duelo.setGanadorPerdedor(ganadorUUID, perdedorUUID);

        duelosActivos.remove(ganadorUUID);
        duelosActivos.remove(perdedorUUID);
        plugin.getArenaManager().liberarArena(duelo.getArena());

        Player ganador  = plugin.getServer().getPlayer(ganadorUUID);
        Player perdedor = plugin.getServer().getPlayer(perdedorUUID);

        String nomG = ganador  != null ? ganador.getName()  : plugin.getEloManager().getNombre(ganadorUUID);
        String nomP = perdedor != null ? perdedor.getName() : plugin.getEloManager().getNombre(perdedorUUID);

        // Calcular ELO
        int[] cambios = plugin.getEloManager().calcularElo(ganadorUUID, perdedorUUID, duelo);
        int ganK = cambios[0], perdP = cambios[1], nEloG = cambios[2], nEloP = cambios[3];

        // Broadcast
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("  §8⚔ §6§l" + nomG + " §r§fderrota a §c" + nomP + "§f.");
        Bukkit.broadcastMessage("  §8└ §7ELO: §a+" + ganK + " §8· §c-" + perdP);
        Bukkit.broadcastMessage("");

        // Notificar ganador
        if (ganador != null && ganador.isOnline()) {
            ganador.sendTitle("§a§l¡GANASTE!", "§f+" + ganK + " ELO → " + nEloG, 10, 60, 10);
            ganador.sendMessage("");
            ganador.sendMessage("  §8[§a+§8] §a+" + ganK + " ELO §8→ §f" + nEloG +
                    " §8(" + plugin.getEloManager().getRango(nEloG) + "§8)");
            ganador.sendMessage("");
            ganador.playSound(ganador.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            ganador.playSound(ganador.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            lanzarFuegos(ganador.getLocation());
        }

        // Notificar perdedor
        if (perdedor != null && perdedor.isOnline()) {
            perdedor.sendTitle("§c§lPERDISTE", "§f-" + perdP + " ELO → " + nEloP, 10, 60, 10);
            perdedor.sendMessage("");
            perdedor.sendMessage("  §8[§c-§8] §c-" + perdP + " ELO §8→ §f" + nEloP +
                    " §8(" + plugin.getEloManager().getRango(nEloP) + "§8)");
            perdedor.sendMessage("");
            perdedor.playSound(perdedor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);

            // Loot time si no keepinv
            boolean keepP = ganadorUUID.equals(duelo.getJugador1()) ? duelo.isKeepinv2() : duelo.isKeepinv1();
            if (!keepP) {
                // Modo real: 90s para recoger loot
                lootCountdown(perdedor, perdedorUUID, 90);
            } else {
                // Práctica: regresar en 3s
                new BukkitRunnable() {
                    @Override public void run() {
                        if (perdedor.isOnline()) teleportarRegreso(perdedor);
                    }
                }.runTaskLater(plugin, 60L);
            }
        }

        // Regresar ganador en 3s
        if (ganador != null) {
            new BukkitRunnable() {
                @Override public void run() {
                    if (ganador.isOnline()) teleportarRegreso(ganador);
                }
            }.runTaskLater(plugin, 60L);
        }

        // Anuncio de racha
        int streak = plugin.getEloManager().getStreak(ganadorUUID);
        if (streak == 5 || streak == 10 || (streak >= 15 && streak % 5 == 0)) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("  §6★ §e§l" + nomG + "§6 lleva §e§l" + streak + " kills §6sin morir! §8⚔");
            Bukkit.broadcastMessage("");
            Bukkit.getOnlinePlayers().forEach(p ->
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1f));
        }

        plugin.getDataManager().save();
    }

    private void lootCountdown(Player p, UUID uuid, int segundos) {
        duelo_loot.add(uuid);
        p.sendMessage("  §8[§e⚠§8] §eTienes §6" + segundos + "s §epara recoger tus ítems.");
        new BukkitRunnable() {
            int restante = segundos;
            @Override public void run() {
                if (!p.isOnline() || restante <= 0) {
                    duelo_loot.remove(uuid);
                    if (p.isOnline()) {
                        p.sendMessage("  §8[§c!§8] §cTiempo de loot terminado.");
                        teleportarRegreso(p);
                    }
                    cancel();
                    return;
                }
                int m = restante / 60, s = restante % 60;
                String time = m + ":" + (s < 10 ? "0" : "") + s;
                p.sendActionBar("§eLoot: §6" + time + " §8| §7Recoge tus ítems");
                restante--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private final Set<UUID> duelo_loot = new HashSet<>();
    public boolean estaEnLoot(UUID uuid) { return duelo_loot.contains(uuid); }

    private void teleportarRegreso(Player p) {
        String world = plugin.getConfig().getString("duelos.spawn-regreso.world", "Lobby");
        double x = plugin.getConfig().getDouble("duelos.spawn-regreso.x", 0);
        double y = plugin.getConfig().getDouble("duelos.spawn-regreso.y", 67);
        double z = plugin.getConfig().getDouble("duelos.spawn-regreso.z", -59.5);
        float yaw = (float) plugin.getConfig().getDouble("duelos.spawn-regreso.yaw", 0);
        float pitch = (float) plugin.getConfig().getDouble("duelos.spawn-regreso.pitch", 0);
        World w = Bukkit.getWorld(world);
        if (w != null) p.teleport(new Location(w, x, y, z, yaw, pitch));
    }

    private void lanzarFuegos(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.YELLOW, Color.ORANGE)
                .withFade(Color.WHITE)
                .trail(true).flicker(true).build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    // ─── HP Bar ───────────────────────────────────────────────────────────
    private void iniciarHPBar() {
        new BukkitRunnable() {
            @Override public void run() {
                for (Map.Entry<UUID, Duelo> entry : new HashMap<>(duelosActivos).entrySet()) {
                    Duelo d = entry.getValue();
                    if (d.getEstado() != Duelo.Estado.ACTIVO) continue;
                    Player p1 = plugin.getServer().getPlayer(d.getJugador1());
                    Player p2 = plugin.getServer().getPlayer(d.getJugador2());
                    if (p1 == null || p2 == null) continue;

                    long elapsed = (System.currentTimeMillis() - d.getInicioMs()) / 1000;
                    int m = (int)(elapsed / 60), s = (int)(elapsed % 60);
                    String timer = m + ":" + (s < 10 ? "0" : "") + s;

                    p1.sendActionBar(construirHPBar(p1, p2, timer));
                    p2.sendActionBar(construirHPBar(p2, p1, timer));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private String construirHPBar(Player yo, Player rival, String timer) {
        double hp = rival.getHealth();
        double maxHp = rival.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        int pct = (int) Math.round(hp / maxHp * 10);
        String col = pct > 6 ? "§a" : pct > 3 ? "§e" : "§c";
        StringBuilder bar = new StringBuilder(col);
        for (int i = 0; i < pct; i++) bar.append("❤");
        bar.append("§8");
        for (int i = pct; i < 10; i++) bar.append("❤");
        return "§f" + rival.getName() + " " + bar + " §8| §f" + String.format("%.1f", hp/2) + "HP §8| §7⏱ " + timer;
    }

    // ─── Utilidades ───────────────────────────────────────────────────────
    public boolean estaEnDuelo(UUID uuid)   { return duelosActivos.containsKey(uuid); }
    public Duelo getDuelo(UUID uuid)        { return duelosActivos.get(uuid); }
    public Collection<Duelo> getDuelos()    { return duelosActivos.values(); }

    public void terminarTodosLosDuelos() {
        Set<UUID> todos = new HashSet<>(duelosActivos.keySet());
        for (UUID uuid : todos) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) p.sendMessage("§c[Duelos] El servidor se reinicia. Duelo cancelado.");
        }
        duelosActivos.clear();
    }

    public List<String> getComandosBloqueados() {
        return plugin.getConfig().getStringList("duelos.comandos-bloqueados");
    }
}
