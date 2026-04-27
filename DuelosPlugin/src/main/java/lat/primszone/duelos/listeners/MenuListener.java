package lat.primszone.duelos.listeners;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Duelo;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

public class MenuListener implements Listener {

    private final DuelosPlugin plugin;

    public MenuListener(DuelosPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String menu = plugin.getMenuManager().getMenuAbierto(p.getUniqueId());
        if (menu == null) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        int slot = e.getRawSlot();
        Material type = e.getCurrentItem().getType();
        UUID uuid = p.getUniqueId();

        // ── MENÚ PRINCIPAL ────────────────────────────────────────────────
        if (menu.equals("PRINCIPAL")) {
            switch (slot) {
                case 10 -> { // NORMAL
                    if (plugin.getDuelManager().estaEnDuelo(uuid) || plugin.getQueueManager().estaEnCola(uuid)) {
                        p.sendMessage("§8[§c!§8] §cYa estás en un duelo o cola."); return;
                    }
                    boolean entro = plugin.getQueueManager().entrarCola(p, Duelo.Modo.NORMAL);
                    if (entro) {
                        p.closeInventory();
                        p.sendMessage("  §8[§a⚔§8] §a¡Entraste a la cola §fNORMAL§a! Buscando rival...");
                        p.sendActionBar("§a§l⚔ §aBuscando duelo NORMAL...");
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                }
                case 12 -> { // NO CRIST/MAZA
                    if (plugin.getDuelManager().estaEnDuelo(uuid) || plugin.getQueueManager().estaEnCola(uuid)) {
                        p.sendMessage("§8[§c!§8] §cYa estás en un duelo o cola."); return;
                    }
                    boolean entro = plugin.getQueueManager().entrarCola(p, Duelo.Modo.NOCRISTMAZA);
                    if (entro) {
                        p.closeInventory();
                        p.sendMessage("  §8[§5⚔§8] §5¡Entraste a la cola §fNO CRYSTAL/MACE§5! Buscando rival...");
                        p.sendActionBar("§5§l⚔ §5Buscando duelo NO CRYSTAL/MACE...");
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                }
                case 14, 29, 31 -> { // HISTORIAL / STATS
                    p.closeInventory();
                    plugin.getMenuManager().abrirStats(p, uuid);
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1f);
                }
                case 16, 33 -> { // RANKING
                    p.closeInventory();
                    plugin.getMenuManager().abrirRanking(p);
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1f);
                }
                case 27 -> { // TOGGLE SKILLS
                    plugin.getEloManager().toggleSkillsOff(uuid);
                    boolean skillsOff = plugin.getEloManager().isSkillsOff(uuid);
                    p.playSound(p.getLocation(), skillsOff ? Sound.BLOCK_NOTE_BLOCK_BASS : Sound.BLOCK_NOTE_BLOCK_PLING, 1f, skillsOff ? 0.8f : 1.2f);
                    if (skillsOff) {
                        p.sendMessage("  §8[§c⚔§8] §cSkills §8desactivadas §8(§c-40%% ELO§8).");
                    } else {
                        p.sendMessage("  §8[§a⚔§8] §aSkills activadas §8(sin penalización).");
                    }
                    p.closeInventory();
                    plugin.getMenuManager().abrirMenuPrincipal(p);
                }
                case 35 -> { // TOGGLE KEEPINV
                    plugin.getEloManager().toggleKeepin(uuid);
                    boolean ahora = plugin.getEloManager().isKeepin(uuid);
                    p.playSound(p.getLocation(), ahora ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BASS, 1f, ahora ? 1.2f : 0.8f);
                    p.closeInventory();
                    plugin.getMenuManager().abrirMenuPrincipal(p);
                }
            }
        }
        // ── RANKING ───────────────────────────────────────────────────────
        else if (menu.equals("RANKING")) {
            if (slot == 40) { // Volver
                p.closeInventory();
                plugin.getMenuManager().abrirMenuPrincipal(p);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1f);
            } else if (type == Material.PLAYER_HEAD || type == Material.SKELETON_SKULL) {
                // Ver stats del jugador
                // Por ahora solo cerramos — podrías extraer el UUID del skull
                p.closeInventory();
                plugin.getMenuManager().abrirRanking(p);
            }
        }
        // ── STATS ─────────────────────────────────────────────────────────
        else if (menu.startsWith("STATS")) {
            if (slot == 31) { // Volver
                p.closeInventory();
                plugin.getMenuManager().abrirMenuPrincipal(p);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1f);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            plugin.getMenuManager().cerrarMenu(p.getUniqueId());
        }
    }
}
