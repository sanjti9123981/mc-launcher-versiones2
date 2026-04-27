package lat.primszone.duelos.models;

import org.bukkit.Location;

public class Arena {
    private final String nombre;
    private Location spawn1;
    private Location spawn2;
    private String schematic; // nombre del .schem en FAWE

    public Arena(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre()          { return nombre; }
    public Location getSpawn1()        { return spawn1; }
    public Location getSpawn2()        { return spawn2; }
    public String getSchematic()       { return schematic; }
    public void setSpawn1(Location l)  { this.spawn1 = l; }
    public void setSpawn2(Location l)  { this.spawn2 = l; }
    public void setSchematic(String s) { this.schematic = s; }

    public boolean estaCompleta() {
        return spawn1 != null && spawn2 != null;
    }
}
