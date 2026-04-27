package lat.primszone.duelos.listeners;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Duelo;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.List;
import java.util.UUID;

public class DuelListener implements Listener {

    private final DuelosPlugin plugin;

    public DuelListener(DuelosPlugin plugin) { this.plugin = plugin; }

    // ─── Registrar nombre al entrar ───────────────────────────────────────
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getEloManager().setNombre(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    }

    // ─── Muerte en duelo ──────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        if (!plugin.getDuelManager().estaEnDuelo(victim.getUniqueId())) return;

        Duelo duelo = plugin.getDuelManager().getDuelo(victim.getUniqueId());
        if (duelo == null || duelo.getEstado() != Duelo.Estado.ACTIVO) return;

        // Cancelar drops si keepinv
        boolean keepVictim = victim.getUniqueId().equals(duelo.getJugador1())
                ? duelo.isKeepinv1() : duelo.isKeepinv2();
        if (keepVictim) {
            e.setDroppedExp(0);
            e.getDrops().clear();
            e.setKeepInventory(true);
        }
        e.setDeathMessage(null);

        UUID ganadorUUID  = duelo.getRival(victim.getUniqueId());
        UUID perdedorUUID = victim.getUniqueId();

        plugin.getDuelManager().terminarDuelo(ganadorUUID, perdedorUUID, "muerte");
    }

    // ─── Bloquear daño de cristales/maza en modo nocristmaza ──────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!plugin.getDuelManager().estaEnDuelo(victim.getUniqueId())) return;

        Duelo duelo = plugin.getDuelManager().getDuelo(victim.getUniqueId());
        if (duelo == null || duelo.getModo() != Duelo.Modo.NOCRISTMAZA) return;

        Entity damager = e.getDamager();

        // Bloquear cristal del End
        if (damager.getType() == EntityType.END_CRYSTAL) {
            e.setCancelled(true);
            if (victim instanceof Player) {
                ((Player) e.getDamager().getPassengers().isEmpty() ? null : e.getDamager())
                        .hashCode(); // noop, just checking
            }
            // Notificar
            Player attacker = null;
            if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                attacker = findNearbyDueler(victim, duelo);
            }
            e.setCancelled(true);
            return;
        }

        // Bloquear Maza (Mace tiene atributo de daño especial)
        if (damager instanceof Player atk) {
            Material itemInHand = atk.getInventory().getItemInMainHand().getType();
            if (itemInHand == Material.MACE) {
                if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                        || e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    e.setCancelled(true);
                    atk.sendMessage("§8[§5§lNO CRYSTAL/MACE§8] §cLa Maza está deshabilitada en este modo.");
                }
            }
        }
    }

    private Player findNearbyDueler(Player victim, Duelo duelo) {
        UUID rivalUUID = duelo.getRival(victim.getUniqueId());
        return plugin.getServer().getPlayer(rivalUUID);
    }

    // ─── Bloquear comandos en duelo ───────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        boolean enDuelo = plugin.getDuelManager().estaEnDuelo(uuid);
        boolean enLoot  = plugin.getDuelManager().estaEnLoot(uuid);

        if (!enDuelo && !enLoot) return;
        if (p.isOp()) return;

        String cmd = e.getMessage().substring(1).split(" ")[0].toLowerCase();
        List<String> bloqueados = plugin.getDuelManager().getComandosBloqueados();
        if (bloqueados.contains(cmd)) {
            e.setCancelled(true);
            p.sendMessage("§8[§c⚔§8] §cNo puedes usar §e/" + cmd + "§c en un duelo.");
        }
    }

    // ─── Desconexión = derrota ────────────────────────────────────────────
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        // Salir de cola
        plugin.getQueueManager().salirCola(uuid);
        plugin.getQueueManager().limpiarRetos(uuid);

        // Si en duelo activo → pierde
        if (plugin.getDuelManager().estaEnDuelo(uuid)) {
            Duelo duelo = plugin.getDuelManager().getDuelo(uuid);
            if (duelo != null && duelo.getEstado() == Duelo.Estado.ACTIVO) {
                UUID rival = duelo.getRival(uuid);
                plugin.getDuelManager().terminarDuelo(rival, uuid, "disconnect");
                Player rivalP = plugin.getServer().getPlayer(rival);
                if (rivalP != null)
                    rivalP.sendMessage("§8[§c!§8] §e" + p.getName() + "§c se desconectó. ¡Ganaste!");
            }
        }
    }

    // ─── Cancelar food y fall damage en countdown ─────────────────────────
    @EventHandler
    public void onDamageGeneral(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!plugin.getDuelManager().estaEnDuelo(p.getUniqueId())) return;
        Duelo d = plugin.getDuelManager().getDuelo(p.getUniqueId());
        if (d != null && d.getEstado() == Duelo.Estado.COUNTDOWN) {
            e.setCancelled(true);
        }
    }
}
