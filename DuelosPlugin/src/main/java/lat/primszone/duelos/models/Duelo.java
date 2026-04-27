package lat.primszone.duelos.models;

import org.bukkit.entity.Player;
import java.util.UUID;

public class Duelo {

    public enum Estado { COUNTDOWN, ACTIVO, LOOT, TERMINADO }
    public enum Modo   { NORMAL, NOCRISTMAZA }

    private final UUID jugador1;
    private final UUID jugador2;
    private final String arena;
    private final Modo modo;
    private Estado estado;
    private UUID ganador;
    private UUID perdedor;
    private long inicioMs;
    private boolean keepinv1;
    private boolean keepinv2;

    public Duelo(Player p1, Player p2, String arena, Modo modo) {
        this.jugador1 = p1.getUniqueId();
        this.jugador2 = p2.getUniqueId();
        this.arena    = arena;
        this.modo     = modo;
        this.estado   = Estado.COUNTDOWN;
        this.inicioMs = System.currentTimeMillis();
    }

    public UUID getJugador1()   { return jugador1; }
    public UUID getJugador2()   { return jugador2; }
    public String getArena()    { return arena; }
    public Modo getModo()       { return modo; }
    public Estado getEstado()   { return estado; }
    public void setEstado(Estado e) { this.estado = e; }
    public UUID getGanador()    { return ganador; }
    public UUID getPerdedor()   { return perdedor; }
    public long getInicioMs()   { return inicioMs; }
    public boolean isKeepinv1() { return keepinv1; }
    public boolean isKeepinv2() { return keepinv2; }
    public void setKeepinv1(boolean v) { this.keepinv1 = v; }
    public void setKeepinv2(boolean v) { this.keepinv2 = v; }

    public void setGanadorPerdedor(UUID g, UUID p) {
        this.ganador  = g;
        this.perdedor = p;
    }

    public boolean tieneJugador(UUID uuid) {
        return jugador1.equals(uuid) || jugador2.equals(uuid);
    }

    public UUID getRival(UUID uuid) {
        return jugador1.equals(uuid) ? jugador2 : jugador1;
    }
}
