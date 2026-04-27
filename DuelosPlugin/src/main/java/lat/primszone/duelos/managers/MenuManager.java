package lat.primszone.duelos.managers;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Duelo;
import lat.primszone.duelos.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class MenuManager {

    private final DuelosPlugin plugin;
    // Inventarios abiertos: UUID del jugador → nombre del menú
    private final Map<UUID, String> menuAbierto = new HashMap<>();

    public MenuManager(DuelosPlugin plugin) { this.plugin = plugin; }

    // ─────────────────────────────────────────────────────────────────────
    // MENÚ PRINCIPAL
    // ─────────────────────────────────────────────────────────────────────
    public void abrirMenuPrincipal(Player p) {
        EloManager elo = plugin.getEloManager();
        QueueManager q = plugin.getQueueManager();
        UUID uuid = p.getUniqueId();
        int eloVal  = elo.getElo(uuid);
        int wins    = elo.getWins(uuid);
        int losses  = elo.getLosses(uuid);
        int streak  = elo.getStreak(uuid);
        String rango = elo.getRango(eloVal);
        String col   = elo.getColor(eloVal);
        String prog  = elo.getProgBar(eloVal);
        boolean keepinv = elo.isKeepin(uuid);

        boolean enDuelo = plugin.getDuelManager().estaEnDuelo(uuid);
        boolean enCola  = q.estaEnCola(uuid);

        Inventory inv = Bukkit.createInventory(null, 36, "§8§l⚔ §f§lDUELOS §8§l⚔ §7PrimsZone");

        // Fondo gris
        ItemStack fondo = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("§r").build();
        ItemStack negro = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§r").build();
        ItemStack rojo  = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).name("§r").build();
        for (int i = 0; i < 36; i++) inv.setItem(i, fondo);
        for (int i = 0; i < 9; i++) inv.setItem(i, negro);
        for (int i = 27; i < 36; i++) inv.setItem(i, negro);
        inv.setItem(0, rojo); inv.setItem(8, rojo);
        inv.setItem(27, rojo); inv.setItem(35, rojo);

        // ── Modo NORMAL (slot 10) ──
        int qNormal = q.getTamanoCola(Duelo.Modo.NORMAL);
        ItemStack normal;
        if (enDuelo || enCola) {
            normal = new ItemBuilder(Material.BARRIER)
                    .name("§cEn duelo o cola activa")
                    .lore("§7Termina tu partida actual primero.").build();
        } else {
            normal = new ItemBuilder(Material.END_CRYSTAL)
                    .name("§c§lNORMAL §8— §f1v1")
                    .lore("§7","§7Modo sin restricciones.","§7Todos los ítems permitidos.","§7",
                          "§d§lMultiplicador de ELO: §fx1","§7",
                          "§8En cola: §e"+qNormal+"  §8Jugando: §e0","§7",
                          "§a§lHaz clic para competir.","§7").build();
        }
        inv.setItem(10, normal);

        // ── Modo NO CRIST/MAZA (slot 12) ──
        int qNoCrist = q.getTamanoCola(Duelo.Modo.NOCRISTMAZA);
        ItemStack nocrist;
        if (enDuelo || enCola) {
            nocrist = new ItemBuilder(Material.BARRIER)
                    .name("§cEn duelo o cola activa")
                    .lore("§7Termina tu partida actual primero.").build();
        } else {
            nocrist = new ItemBuilder(Material.NETHERITE_SWORD)
                    .name("§5§lNO CRYSTAL/MACE §8— §f1v1")
                    .lore("§7","§7Los siguientes ítems están","§7deshabilitados:",
                          "§c  - Cristal del End","§c  - Maza","§c  - Tótem de inmortalidad","§7",
                          "§d§lMultiplicador de ELO: §fx1.5","§7",
                          "§8En cola: §e"+qNoCrist+"  §8Jugando: §e0","§7",
                          "§a§lHaz clic para competir.","§7").build();
        }
        inv.setItem(12, nocrist);

        // ── Historial (slot 14) ──
        inv.setItem(14, new ItemBuilder(Material.SPYGLASS)
                .name("§b§lHistorial de partidas")
                .lore("§7","§8Victorias: §a"+wins,"§8Derrotas:  §c"+losses,"§8Racha:     §6"+streak,
                      "§7","§8ELO: "+col+"§l"+eloVal+"  §8("+rango+"§8)","§7"+prog,"§7").build());

        // ── Partidas en curso (slot 16) ──
        inv.setItem(16, new ItemBuilder(Material.CLOCK)
                .name("§e§lPartidas en curso")
                .lore("§7","§7Ver ranking y estadísticas globales.","§7","§aClic para ver.","§7").build());

        // Fila inferior ──
        // Skills toggle (slot 27)
        boolean skillsOff = elo.isSkillsOff(uuid);
        if (skillsOff) {
            inv.setItem(27, new ItemBuilder(Material.BOOK)
                    .name("§8§lSKILLS §8(DESACTIVADAS)")
                    .lore("§7","§c✘ §7Habilidades de AuraSkills desactivadas.",
                          "§c§lPenalización: §c-40%% de ELO ganado","§7",
                          "§7Clic para §aactivar§7 skills.","§7").build());
        } else {
            inv.setItem(27, new ItemBuilder(Material.ENCHANTED_BOOK)
                    .name("§a§lSKILLS §8(ACTIVAS)")
                    .lore("§7","§a✔ §7Habilidades de AuraSkills activas.",
                          "§7","§7Clic para §cdesactivar §8(§c-40%% ELO§8)§7.","§7").build());
        }

        inv.setItem(29, new ItemBuilder(Material.FILLED_MAP)
                .name("§f§lEstadísticas completas")
                .lore("§7","§8Wins:   §a"+wins,"§8Losses: §c"+losses,"§8Racha:  §6"+streak,"§7").build());

        // Cabeza del jugador (slot 31)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(p);
        sm.setDisplayName("§f§l" + p.getName());
        sm.setLore(Arrays.asList("§7", rango, col+"§l"+eloVal+" ELO",
                "§7Pos. global: §e#" + elo.getPosicionGlobal(uuid), "§7"+prog, "§7"));
        head.setItemMeta(sm);
        inv.setItem(31, head);

        // Trofeo ranking (slot 33)
        inv.setItem(33, new ItemBuilder(Material.GOLDEN_HELMET)
                .name("§6§lRanking Global")
                .lore("§7","§7Top 10 jugadores por ELO.","§7Tu posición: §e#"+elo.getPosicionGlobal(uuid),"§7").build());

        // Toggle keepinv (slot 35)
        if (keepinv) {
            inv.setItem(35, new ItemBuilder(Material.TOTEM_OF_UNDYING)
                    .name("§a§lCONSERVAR INVENTARIO")
                    .lore("§7","§aConservas tu inventario si mueres.","§c§lPenalización: §c-30%% de ELO ganado","§7",
                          "§7Clic para desactivar.","§a§lObtén un multiplicador de x2 si ganas.","§7").build());
        } else {
            inv.setItem(35, new ItemBuilder(Material.TOTEM_OF_UNDYING)
                    .name("§c§lCONSERVAR INVENTARIO §8(OFF)")
                    .lore("§7","§cNo conservas tu inventario al morir.","§a§lMultiplicador x2 de ELO si ganas.","§7",
                          "§7Clic para activar (§c-30%% ELO§7)","§7").build());
        }

        menuAbierto.put(uuid, "PRINCIPAL");
        p.openInventory(inv);
    }

    // ─────────────────────────────────────────────────────────────────────
    // MENÚ RANKING
    // ─────────────────────────────────────────────────────────────────────
    public void abrirRanking(Player p) {
        EloManager elo = plugin.getEloManager();
        Inventory inv = Bukkit.createInventory(null, 45, "§6§l★ RANKING ELO §8» Top 10");

        ItemStack negro   = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§r").build();
        ItemStack amarillo= new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§r").build();
        ItemStack naranja = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).name("§6§l★").build();
        for (int i = 0; i < 45; i++) inv.setItem(i, negro);
        for (int i = 0; i < 9; i++)  inv.setItem(i, amarillo);
        for (int i = 36; i < 45; i++) inv.setItem(i, amarillo);
        inv.setItem(0, naranja); inv.setItem(8, naranja);
        inv.setItem(36, naranja); inv.setItem(44, naranja);

        inv.setItem(40, new ItemBuilder(Material.ARROW).name("§7← Volver").build());

        int[] slots = {10,11,12,13,14,19,20,21,22,23};
        String[] icons = {"§6§l#1 ✦","§7§l#2","§c§l#3","§f#4","§f#5","§f#6","§f#7","§f#8","§f#9","§f#10"};

        List<Map.Entry<UUID, Integer>> top = elo.getTop10();
        for (int i = 0; i < top.size() && i < slots.length; i++) {
            UUID uid = top.get(i).getKey();
            int eloV = top.get(i).getValue();
            String rango = elo.getRango(eloV);
            String colS  = elo.getColor(eloV);
            String nom   = elo.getNombre(uid);
            int winsV    = elo.getWins(uid);
            int streakV  = elo.getStreak(uid);

            ItemStack skull = new ItemStack(Material.SKELETON_SKULL);
            Player online = Bukkit.getPlayer(uid);
            if (online != null) {
                skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta sm = (SkullMeta) skull.getItemMeta();
                sm.setOwningPlayer(online);
                skull.setItemMeta(sm);
            }
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            sm.setDisplayName(icons[i] + " §f" + nom);
            sm.setLore(Arrays.asList("§7", rango + "  " + colS + eloV + " ELO",
                    "§8Wins: §a"+winsV+"  §8Racha: §6"+streakV, "§7"));
            skull.setItemMeta(sm);
            inv.setItem(slots[i], skull);
        }

        menuAbierto.put(p.getUniqueId(), "RANKING");
        p.openInventory(inv);
    }

    // ─────────────────────────────────────────────────────────────────────
    // MENÚ STATS
    // ─────────────────────────────────────────────────────────────────────
    public void abrirStats(Player viewer, UUID targetUUID) {
        EloManager elo = plugin.getEloManager();
        String nom    = elo.getNombre(targetUUID);
        int eloV      = elo.getElo(targetUUID);
        int wins      = elo.getWins(targetUUID);
        int losses    = elo.getLosses(targetUUID);
        int kills     = elo.getKills(targetUUID);
        int deaths    = elo.getDeaths(targetUUID);
        int streak    = elo.getStreak(targetUUID);
        String rango  = elo.getRango(eloV);
        String col    = elo.getColor(eloV);
        String prog   = elo.getProgBar(eloV);
        int partidas  = wins + losses;
        String kd     = deaths > 0 ? String.format("%.2f", (double)kills/deaths) : "∞";
        String wr     = partidas > 0 ? (int)Math.round(wins * 100.0 / partidas) + "%" : "0%";

        Inventory inv = Bukkit.createInventory(null, 36, "§b§l● STATS §8» " + nom);
        ItemStack negro = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§r").build();
        ItemStack azul  = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).name("§r").build();
        ItemStack cian  = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name("§r").build();
        for (int i = 0; i < 36; i++) inv.setItem(i, negro);
        for (int i = 0; i < 9; i++)  inv.setItem(i, azul);
        for (int i = 27; i < 36; i++) inv.setItem(i, azul);
        inv.setItem(0, cian); inv.setItem(8, cian); inv.setItem(27, cian); inv.setItem(35, cian);

        // Cabeza
        ItemStack skull = new ItemStack(Material.SKELETON_SKULL);
        Player online = Bukkit.getPlayer(targetUUID);
        if (online != null) {
            skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta smm = (SkullMeta) skull.getItemMeta();
            smm.setOwningPlayer(online);
            skull.setItemMeta(smm);
        }
        SkullMeta sm = (SkullMeta) skull.getItemMeta();
        sm.setDisplayName("§f§l" + nom);
        sm.setLore(Arrays.asList("§7", rango, col+"§l"+eloV+" ELO",
                "§7Pos. global: §e#"+elo.getPosicionGlobal(targetUUID), "§7"+prog, "§7"));
        skull.setItemMeta(sm);
        inv.setItem(13, skull);

        inv.setItem(10, new ItemBuilder(Material.NETHER_STAR).name("§e§lELO & RANGO")
                .lore("§7","§7Rango: "+rango,"§7ELO:   "+col+"§l"+eloV,
                      "§7Pos:   §e#"+elo.getPosicionGlobal(targetUUID),"§7","§7Progreso:","§7"+prog,"§7").build());
        inv.setItem(11, new ItemBuilder(Material.IRON_SWORD).name("§c§lCOMBATE")
                .lore("§7","§aKills:   §f"+kills,"§cMuertes: §f"+deaths,"§eK/D:     §f"+kd,"§7").build());
        inv.setItem(15, new ItemBuilder(Material.SHIELD).name("§d§lDUELOS")
                .lore("§7","§aVictorias: §f"+wins,"§cDerrotas:  §f"+losses,
                      "§ePartidas:  §f"+partidas,"§bWinRate:   §f"+wr,"§7").build());
        inv.setItem(16, new ItemBuilder(Material.BLAZE_ROD).name("§6§lRACHA §8» §f"+streak)
                .lore("§7","§6"+streak+" kills sin morir.","§7").build());
        inv.setItem(31, new ItemBuilder(Material.ARROW).name("§7← Volver").build());

        menuAbierto.put(viewer.getUniqueId(), "STATS:" + targetUUID);
        viewer.openInventory(inv);
    }

    public String getMenuAbierto(UUID uuid) { return menuAbierto.get(uuid); }
    public void cerrarMenu(UUID uuid) { menuAbierto.remove(uuid); }
}
