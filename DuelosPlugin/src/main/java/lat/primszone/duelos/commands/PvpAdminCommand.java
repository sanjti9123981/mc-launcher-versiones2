package lat.primszone.duelos.commands;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Arena;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvpAdminCommand implements CommandExecutor {
    private final DuelosPlugin plugin;
    public PvpAdminCommand(DuelosPlugin p) { this.plugin = p; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("duelos.admin")) { sender.sendMessage("§cSin permiso."); return true; }
        if (args.length == 0) {
            sender.sendMessage("§e/pvpadmin <reset|setelo|reload> [jugador] [valor]");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reset" -> {
                if (args.length < 2) { sender.sendMessage("§c/pvpadmin reset <jugador>"); return true; }
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
                java.util.UUID uuid = target.getUniqueId();
                plugin.getEloManager().setElo(uuid, plugin.getConfig().getInt("duelos.elo-inicial", 1000));
                plugin.getEloManager().setWins(uuid, 0);
                plugin.getEloManager().setLosses(uuid, 0);
                plugin.getEloManager().setKills(uuid, 0);
                plugin.getEloManager().setDeaths(uuid, 0);
                plugin.getEloManager().setStreak(uuid, 0);
                sender.sendMessage("§a[PvP] Stats de §e" + args[1] + "§a reseteadas.");
            }
            case "setelo" -> {
                if (args.length < 3) { sender.sendMessage("§c/pvpadmin setelo <jugador> <valor>"); return true; }
                OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[1]);
                try {
                    int val = Integer.parseInt(args[2]);
                    plugin.getEloManager().setElo(target.getUniqueId(), val);
                    sender.sendMessage("§a[PvP] ELO de §e" + args[1] + "§a = §e" + val + "§a.");
                } catch (NumberFormatException ex) {
                    sender.sendMessage("§cValor inválido.");
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage("§a[PvP] Config recargada.");
            }
            default -> sender.sendMessage("§e/pvpadmin <reset|setelo|reload> [jugador] [valor]");
        }
        return true;
    }
}
