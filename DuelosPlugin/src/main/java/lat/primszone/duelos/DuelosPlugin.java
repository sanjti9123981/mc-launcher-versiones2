package lat.primszone.duelos;

import lat.primszone.duelos.commands.ArenaCommand;
import lat.primszone.duelos.commands.DueloCommand;
import lat.primszone.duelos.commands.DuelosCommand;
import lat.primszone.duelos.commands.PvpAdminCommand;
import lat.primszone.duelos.listeners.DuelListener;
import lat.primszone.duelos.listeners.MenuListener;
import lat.primszone.duelos.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class DuelosPlugin extends JavaPlugin {

    private static DuelosPlugin instance;

    private EloManager eloManager;
    private ArenaManager arenaManager;
    private QueueManager queueManager;
    private DuelManager duelManager;
    private MenuManager menuManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Managers
        this.dataManager    = new DataManager(this);
        this.eloManager     = new EloManager(this);
        this.arenaManager   = new ArenaManager(this);
        this.queueManager   = new QueueManager(this);
        this.duelManager    = new DuelManager(this);
        this.menuManager    = new MenuManager(this);

        dataManager.load();

        // Listeners
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // Commands
        getCommand("duelos").setExecutor(new DuelosCommand(this));
        getCommand("duelo").setExecutor(new DueloCommand(this));
        getCommand("pvpadmin").setExecutor(new PvpAdminCommand(this));
        getCommand("arenaduelos").setExecutor(new ArenaCommand(this));

        getLogger().info("§a✦ DuelosPrimsZone v" + getDescription().getVersion() + " activado.");
    }

    @Override
    public void onDisable() {
        if (duelManager != null) duelManager.terminarTodosLosDuelos();
        if (dataManager != null) dataManager.save();
        getLogger().info("§c✦ DuelosPrimsZone desactivado.");
    }

    public static DuelosPlugin getInstance() { return instance; }
    public EloManager getEloManager()        { return eloManager; }
    public ArenaManager getArenaManager()    { return arenaManager; }
    public QueueManager getQueueManager()    { return queueManager; }
    public DuelManager getDuelManager()      { return duelManager; }
    public MenuManager getMenuManager()      { return menuManager; }
    public DataManager getDataManager()      { return dataManager; }
}
