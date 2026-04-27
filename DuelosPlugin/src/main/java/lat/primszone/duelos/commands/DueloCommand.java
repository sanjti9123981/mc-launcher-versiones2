package lat.primszone.duelos.commands;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Arena;
import lat.primszone.duelos.models.Duelo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// ────────────────────────────────────────────────────
// /duelo aceptar|rechazar|salir|retar <jugador>
// ────────────────────────────────────────────────────
class DueloCommandImpl implements CommandExecutor {
    private final DuelosPlugin plugin;
    DueloCommandImpl(DuelosPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("§cSolo jugadores."); return true; }
        if (args.length == 0) {
            p.sendMessage("§e/duelo <retar|aceptar|rechazar|salir> [jugador] [modo]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "retar" -> {
                if (args.length < 2) { p.sendMessage("§cUso: /duelo retar <jugador> [normal|nocristmaza]"); return true; }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) { p.sendMessage("§cJugador no encontrado."); return true; }
                if (target == p)    { p.sendMessage("§cNo puedes retarte a ti mismo."); return true; }
                if (plugin.getDuelManager().estaEnDuelo(p.getUniqueId()))      { p.sendMessage("§cYa estás en un duelo."); return true; }
                if (plugin.getDuelManager().estaEnDuelo(target.getUniqueId())) { p.sendMessage("§c" + target.getName() + " ya está en un duelo."); return true; }
                Duelo.Modo modo = Duelo.Modo.NORMAL;
                if (args.length >= 3 && args[2].equalsIgnoreCase("nocristmaza")) modo = Duelo.Modo.NOCRISTMAZA;
                plugin.getQueueManager().enviarReto(p, target, modo);
                p.sendMessage("  §8[§e⚔§8] §7Reto enviado a §f" + target.getName() + "§7. (§c/duelo aceptar " + p.getName() + "§7)");
                target.sendMessage("  §8[§e⚔§8] §f" + p.getName() + "§7 te reta a un duelo §f(" + modo.name() + ")§7.");
                target.sendMessage("  §7Escribe §a/duelo aceptar " + p.getName() + " §7para aceptar.");
            }
            case "aceptar" -> {
                if (args.length < 2) { p.sendMessage("§cUso: /duelo aceptar <retador>"); return true; }
                Player retador = plugin.getServer().getPlayer(args[1]);
                if (retador == null) { p.sendMessage("§cJugador no encontrado."); return true; }
                Duelo.Modo modo = plugin.getQueueManager().aceptarReto(p.getUniqueId(), retador.getUniqueId());
                if (modo == null) { p.sendMessage("§cNo tienes reto pendiente de " + retador.getName() + "."); return true; }
                plugin.getDuelManager().iniciarDuelo(retador, p, modo);
            }
            case "rechazar" -> {
                if (args.length < 2) { p.sendMessage("§cUso: /duelo rechazar <retador>"); return true; }
                Player retador = plugin.getServer().getPlayer(args[1]);
                if (retador == null) { p.sendMessage("§cJugador no encontrado."); return true; }
                plugin.getQueueManager().aceptarReto(p.getUniqueId(), retador.getUniqueId());
                p.sendMessage("§7Rechazaste el reto de §f" + retador.getName() + "§7.");
                if (retador.isOnline()) retador.sendMessage("§c" + p.getName() + "§c rechazó tu reto.");
            }
            case "salir", "leave", "rendirse" -> {
                if (!plugin.getDuelManager().estaEnDuelo(p.getUniqueId()) && !plugin.getQueueManager().estaEnCola(p.getUniqueId())) {
                    p.sendMessage("§cNo estás en un duelo ni en cola."); return true;
                }
                if (plugin.getQueueManager().estaEnCola(p.getUniqueId())) {
                    plugin.getQueueManager().salirCola(p.getUniqueId());
                    p.sendMessage("§7Saliste de la cola."); return true;
                }
                Duelo d = plugin.getDuelManager().getDuelo(p.getUniqueId());
                if (d != null) {
                    UUID rival = d.getRival(p.getUniqueId());
                    plugin.getDuelManager().terminarDuelo(rival, p.getUniqueId(), "rendicion");
                    p.sendMessage("§cTe rendiste. Perdiste el duelo.");
                }
            }
            default -> p.sendMessage("§e/duelo <retar|aceptar|rechazar|salir> [jugador]");
        }
        return true;
    }

    private java.util.UUID getUUID(Player p) { return p.getUniqueId(); }
}

// ────────────────────────────────────────────────────
// /pvpadmin reset|setelo|reload
// ────────────────────────────────────────────────────
public class DueloCommand implements CommandExecutor {
    private final DueloCommandImpl impl;
    public DueloCommand(DuelosPlugin p) { this.impl = new DueloCommandImpl(p); }
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) { return impl.onCommand(s, c, l, a); }
}
