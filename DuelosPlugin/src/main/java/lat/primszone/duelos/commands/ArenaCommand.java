package lat.primszone.duelos.commands;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {
    private final DuelosPlugin plugin;
    public ArenaCommand(DuelosPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duelos.admin")) { sender.sendMessage("§cSin permiso."); return true; }
        if (!(sender instanceof Player p)) { sender.sendMessage("§cSolo jugadores para crear arenas."); return true; }
        if (args.length == 0) {
            p.sendMessage("§e/arenaduelos <crear|spawn1|spawn2|schematic|listar|borrar> [nombre]");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "crear" -> {
                if (args.length < 2) { p.sendMessage("§c/arenaduelos crear <nombre>"); return true; }
                plugin.getArenaManager().crearArena(args[1]);
                plugin.getArenaManager().guardar();
                p.sendMessage("§a[Arenas] Arena '§e" + args[1] + "§a' creada. Configura spawn1 y spawn2.");
            }
            case "spawn1" -> {
                if (args.length < 2) { p.sendMessage("§c/arenaduelos spawn1 <nombre>"); return true; }
                Arena arena = plugin.getArenaManager().getArena(args[1]);
                if (arena == null) { p.sendMessage("§cArena no encontrada."); return true; }
                arena.setSpawn1(p.getLocation());
                plugin.getArenaManager().guardar();
                p.sendMessage("§a[Arenas] Spawn 1 de '§e" + args[1] + "§a' guardado aquí.");
            }
            case "spawn2" -> {
                if (args.length < 2) { p.sendMessage("§c/arenaduelos spawn2 <nombre>"); return true; }
                Arena arena = plugin.getArenaManager().getArena(args[1]);
                if (arena == null) { p.sendMessage("§cArena no encontrada."); return true; }
                arena.setSpawn2(p.getLocation());
                plugin.getArenaManager().guardar();
                p.sendMessage("§a[Arenas] Spawn 2 de '§e" + args[1] + "§a' guardado aquí.");
            }
            case "schematic" -> {
                if (args.length < 3) { p.sendMessage("§c/arenaduelos schematic <arena> <fichero>"); return true; }
                Arena arena = plugin.getArenaManager().getArena(args[1]);
                if (arena == null) { p.sendMessage("§cArena no encontrada."); return true; }
                arena.setSchematic(args[2]);
                plugin.getArenaManager().guardar();
                p.sendMessage("§a[Arenas] Schematic de '§e" + args[1] + "§a' = §e" + args[2] + "§a.");
            }
            case "listar" -> {
                p.sendMessage("§e[Arenas disponibles]:");
                for (Arena a : plugin.getArenaManager().getTodasLasArenas()) {
                    String estado = a.estaCompleta() ? "§a✔" : "§c✘ Falta spawn";
                    p.sendMessage("  §f" + a.getNombre() + " " + estado);
                }
            }
            case "borrar" -> {
                if (args.length < 2) { p.sendMessage("§c/arenaduelos borrar <nombre>"); return true; }
                boolean ok = plugin.getArenaManager().borrarArena(args[1]);
                p.sendMessage(ok ? "§a[Arenas] Eliminada '§e" + args[1] + "§a'." : "§cArena no encontrada.");
            }
            default -> p.sendMessage("§e/arenaduelos <crear|spawn1|spawn2|schematic|listar|borrar> [nombre]");
        }
        return true;
    }
}
