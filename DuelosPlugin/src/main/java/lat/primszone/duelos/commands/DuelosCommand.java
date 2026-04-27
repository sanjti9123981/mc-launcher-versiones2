package lat.primszone.duelos.commands;

import lat.primszone.duelos.DuelosPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelosCommand implements CommandExecutor {
    private final DuelosPlugin plugin;
    public DuelosCommand(DuelosPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("§cSolo jugadores."); return true; }
        if (!p.hasPermission("duelos.play")) { p.sendMessage("§cSin permiso."); return true; }
        plugin.getMenuManager().abrirMenuPrincipal(p);
        return true;
    }
}
