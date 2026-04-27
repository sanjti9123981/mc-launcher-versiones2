package lat.primszone.duelos.managers;

import lat.primszone.duelos.DuelosPlugin;
import lat.primszone.duelos.models.Duelo;
import org.bukkit.entity.Player;

import java.util.*;

public class EloManager {

    private final DuelosPlugin plugin;
    // UUID → ELO (en memoria, persistido por DataManager)
    private final Map<UUID, Integer> eloMap    = new HashMap<>();
    private final Map<UUID, Integer> winsMap   = new HashMap<>();
    private final Map<UUID, Integer> lossesMap = new HashMap<>();
    private final Map<UUID, Integer> killsMap  = new HashMap<>();
    private final Map<UUID, Integer> deathsMap = new HashMap<>();
    private final Map<UUID, Integer> streakMap = new HashMap<>();
    private final Map<UUID, Boolean> keepinvMap = new HashMap<>();
    private final Map<UUID, Boolean> skillsOffMap = new HashMap<>();
    private final Map<UUID, String> nombresMap = new HashMap<>();

    public EloManager(DuelosPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Getters / setters ───────────────────────────────────────────────
    public int getElo(UUID uuid)      { return eloMap.getOrDefault(uuid, plugin.getConfig().getInt("duelos.elo-inicial", 1000)); }
    public int getWins(UUID uuid)     { return winsMap.getOrDefault(uuid, 0); }
    public int getLosses(UUID uuid)   { return lossesMap.getOrDefault(uuid, 0); }
    public int getKills(UUID uuid)    { return killsMap.getOrDefault(uuid, 0); }
    public int getDeaths(UUID uuid)   { return deathsMap.getOrDefault(uuid, 0); }
    public int getStreak(UUID uuid)   { return streakMap.getOrDefault(uuid, 0); }
    public boolean isKeepin(UUID uuid){ return keepinvMap.getOrDefault(uuid, false); }

    public void setElo(UUID uuid, int elo)       { eloMap.put(uuid, Math.max(0, elo)); }
    public void setWins(UUID uuid, int v)        { winsMap.put(uuid, v); }
    public void setLosses(UUID uuid, int v)      { lossesMap.put(uuid, v); }
    public void setKills(UUID uuid, int v)       { killsMap.put(uuid, v); }
    public void setDeaths(UUID uuid, int v)      { deathsMap.put(uuid, v); }
    public void setStreak(UUID uuid, int v)      { streakMap.put(uuid, v); }
    public void toggleKeepin(UUID uuid)          { keepinvMap.put(uuid, !isKeepin(uuid)); }
    public void setKeepin(UUID uuid, boolean v)  { keepinvMap.put(uuid, v); }
    public boolean isSkillsOff(UUID uuid)        { return skillsOffMap.getOrDefault(uuid, false); }
    public void toggleSkillsOff(UUID uuid)       { skillsOffMap.put(uuid, !isSkillsOff(uuid)); }
    public void setSkillsOff(UUID uuid, boolean v){ skillsOffMap.put(uuid, v); }

    // ─── Rango ───────────────────────────────────────────────────────────
    public String getRango(int elo) {
        if (elo >= 4000) return "§4§l☠ INMORTAL";
        if (elo >= 3200) return "§b§l◆ DIAMANTE";
        if (elo >= 2500) return "§e§l✦ ORO";
        if (elo >= 1800) return "§f§l▲ PLATA";
        if (elo >= 1200) return "§6§l● BRONCE";
        return "§7§l○ NOVATO";
    }

    public String getColor(int elo) {
        if (elo >= 4000) return "§4";
        if (elo >= 3200) return "§b";
        if (elo >= 2500) return "§e";
        if (elo >= 1800) return "§f";
        if (elo >= 1200) return "§6";
        return "§7";
    }

    public String getProgBar(int elo) {
        if (elo >= 4000) return "§4§lMAX §8· §7INMORTAL";
        int[] limites = {0, 1200, 1800, 2500, 3200, 4000};
        int min = 0, max = 1200;
        for (int i = limites.length - 2; i >= 0; i--) {
            if (elo >= limites[i]) { min = limites[i]; max = limites[i+1]; break; }
        }
        int pct = Math.min(10, Math.max(0, (int) Math.round((elo - min) * 10.0 / (max - min))));
        int falta = max - elo;
        String[] barras = {"§8▰▰▰▰▰▰▰▰▰▰","§a▰§8▰▰▰▰▰▰▰▰▰","§a▰▰§8▰▰▰▰▰▰▰▰",
                "§a▰▰▰§8▰▰▰▰▰▰▰","§a▰▰▰▰§8▰▰▰▰▰▰","§a▰▰▰▰▰§8▰▰▰▰▰",
                "§e▰▰▰▰▰▰§8▰▰▰▰","§e▰▰▰▰▰▰▰§8▰▰▰","§6▰▰▰▰▰▰▰▰§8▰▰",
                "§6▰▰▰▰▰▰▰▰▰§8▰","§a▰▰▰▰▰▰▰▰▰▰"};
        return barras[pct] + " §8(" + falta + " ELO para subir)";
    }

    public int getPosicionGlobal(UUID uuid) {
        int myElo = getElo(uuid);
        int pos = 1;
        for (int e : eloMap.values()) if (e > myElo) pos++;
        return pos;
    }

    // ─── Calcular y aplicar ELO al terminar un duelo ─────────────────────
    public int[] calcularElo(UUID ganadorUUID, UUID perdedorUUID, Duelo duelo) {
        int eK = getElo(ganadorUUID);
        int eP = getElo(perdedorUUID);
        int K  = plugin.getConfig().getInt("duelos.elo-k", 32);

        double expK = 1.0 / (1.0 + Math.pow(10, (eP - eK) / 400.0));
        double expP = 1.0 - expK;

        int ganK = (int) Math.round(K * (1.0 - expK));
        int perdP = (int) Math.round(K * expP);

        // Multiplicador de modo
        double mult = 1.0;
        if (duelo.getModo() == Duelo.Modo.NOCRISTMAZA) mult *= 1.5;

        // Keep inventory
        boolean keepG = ganadorUUID.equals(duelo.getJugador1()) ? duelo.isKeepinv1() : duelo.isKeepinv2();
        if (keepG) {
            mult *= 0.70; // -30% si conserva inventario
        } else {
            mult *= 2.0;  // x2 si no conserva
        }

        // Skills desactivadas: -40% ELO (ambos jugadores deben tenerlo para aplicar)
        boolean skillsOffG = isSkillsOff(ganadorUUID);
        boolean skillsOffP = isSkillsOff(perdedorUUID);
        if (skillsOffG || skillsOffP) {
            mult *= 0.60; // -40%
        }

        ganK  = (int) Math.round(ganK  * mult);
        perdP = (int) Math.round(perdP * 1.0);

        // Aplicar
        setElo(ganadorUUID,  eK + ganK);
        setElo(perdedorUUID, Math.max(0, eP - perdP));

        winsMap.merge(ganadorUUID, 1, Integer::sum);
        lossesMap.merge(perdedorUUID, 1, Integer::sum);
        killsMap.merge(ganadorUUID, 1, Integer::sum);
        deathsMap.merge(perdedorUUID, 1, Integer::sum);
        streakMap.merge(ganadorUUID, 1, Integer::sum);
        streakMap.put(perdedorUUID, 0);

        return new int[]{ganK, perdP, getElo(ganadorUUID), getElo(perdedorUUID)};
    }

    // ─── Top 10 ──────────────────────────────────────────────────────────
    public List<Map.Entry<UUID, Integer>> getTop10() {
        List<Map.Entry<UUID, Integer>> lista = new ArrayList<>(eloMap.entrySet());
        lista.sort((a, b) -> b.getValue() - a.getValue());
        return lista.subList(0, Math.min(10, lista.size()));
    }

    // ─── Persistencia ────────────────────────────────────────────────────
    public Map<UUID, Integer> getEloMap()    { return eloMap; }
    public Map<UUID, Integer> getWinsMap()   { return winsMap; }
    public Map<UUID, Integer> getLossesMap() { return lossesMap; }
    public Map<UUID, Integer> getKillsMap()  { return killsMap; }
    public Map<UUID, Integer> getDeathsMap() { return deathsMap; }
    public Map<UUID, Integer> getStreakMap() { return streakMap; }
    public Map<UUID, Boolean> getKeepinvMap(){ return keepinvMap; }
    public Map<UUID, Boolean> getSkillsOffMap(){ return skillsOffMap; }
    public Map<UUID, String> getNombres()    { return nombresMap; }
    public String getNombre(UUID uuid)       { return nombresMap.getOrDefault(uuid, "?"); }
    public void setNombre(UUID uuid, String nom) { nombresMap.put(uuid, nom); }
}
