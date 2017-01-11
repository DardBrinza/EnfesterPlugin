package ru.enfester.plugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Samar
 */
public class Main extends JavaPlugin implements Listener {

    Connection connection;
    Statement statement;
    ResultSet res;
    List<String> mat;

    FileConfiguration config;

    Home home;

    @Override
    public void onEnable() {

        loadPlugin();

        // Регестрируем команды для дома
        home = new Home(this);
        getCommand("sethome").setExecutor(home);
        getCommand("home").setExecutor(home);
        // Загружаем позиции домов
        loadHomes();

    }

    @Override
    public void onDisable() {
        getServer().broadcastMessage("Мой плагин отключен");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            if (!event.getPlayer().hasPlayedBefore()) {
                event.getPlayer().sendMessage(ChatColor.GREEN + "Привет, " + event.getPlayer().getName() + "!");
                event.getPlayer().sendMessage(ChatColor.GOLD + "Ты впервые на сервере, и был отправлен в случайную точку на карте.");
                event.getPlayer().sendMessage(ChatColor.GOLD + "У нас конечно есть спавн /spawn и админ магазин /adminshop");
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Приятной игры на нашем сервере!");
                new RandomTeleport(event.getPlayer());
            }

            res = statement.executeQuery("SELECT * FROM `enefster_plugin_session` WHERE `name`='" + event.getPlayer().getName() + "';");
            if (!res.next()) { // Если нет в таблице то создаем 
                statement.executeUpdate("INSERT INTO `enefster_plugin_session` (`name`, `time_connect`, `time_disconnect`, `time_play`)"
                        + " VALUES ('" + event.getPlayer().getName() + "', CURRENT_TIMESTAMP, NULL, '0');");

            }
            statement.executeUpdate("UPDATE `enefster_plugin_session` SET `time_connect`= CURRENT_TIMESTAMP WHERE `name`='" + event.getPlayer().getName() + "';");

            event.setJoinMessage(event.getPlayer().getName() + " зашел на сервер");
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        try {
            statement.executeUpdate("UPDATE `enefster_plugin_session` SET `time_disconnect`= CURRENT_TIMESTAMP, `time_play` = CURRENT_TIMESTAMP - `time_connect` + `time_play` WHERE `name`='" + event.getPlayer().getName() + "';");

            event.setQuitMessage(event.getPlayer().getName() + " покинул сервер");
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event instanceof Player) {
            ((Player) event).getPlayer().sendMessage(ChatColor.GREEN + "Добро пожаловать домой!");
            event.setRespawnLocation(home.getHome(((Player) event).getPlayer()));
            home.toHome(((Player) event).getPlayer());
        }
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {

        new Home(this).toHome(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("enfester") && player.hasPermission("enfester.admin")) {
            if ("reload".equals(args[0])) {
                reloadPlugin();
                return true;
            }
            if ("help".equals(args[0])) {

                return true;
            }
            if ("rtp".equals(args[0])) {

                if ("rtp".equals(args[1])) {
                    new RandomTeleport(player);
                } else {
                    new RandomTeleport(player);
                }
                return true;
            }
            if ("setspawn".equals(args[0])) {
                Location location = player.getLocation();

                getConfig().set("map.spawn.positionX", location.getX());
                getConfig().set("map.spawn.positionY", location.getY());
                getConfig().set("map.spawn.positionZ", location.getZ());
                getConfig().set("map.spawn.rotation", location.getYaw());
                buildConfig();

                return true;
            }
            if ("setadminshop".equals(args[0])) {
                Location location = player.getLocation();

                getConfig().set("map.adminshop.positionX", location.getX());
                getConfig().set("map.adminshop.positionY", location.getY());
                getConfig().set("map.adminshop.positionZ", location.getZ());
                getConfig().set("map.adminshop.rotation", location.getYaw());
                buildConfig();
                return true;
            }
            if ("mat".equals(args[0])) {
                if ("list".equals(args[1])) {
                    for (String str : mat) {
                        player.sendMessage(str);
                    }
                }
                if ("add".equals(args[1])) {
                    mat.add(args[2]);
                    getConfig().set("chat", mat);
                    buildConfig();
                    player.sendMessage("Матное слово " + args[2] + " добавлено в список.");
                }
                if ("remove".equals(args[1])) {
                    mat.remove(args[2]);
                    getConfig().set("chat", mat);
                    buildConfig();
                    player.sendMessage("Матное слово " + args[2] + " удалено из списка.");
                }
                return true;
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawn")) {

            Location location = new Location(player.getWorld(),
                    (double) getConfig().getInt("map.spawn.positionX"),
                    (double) getConfig().getInt("map.spawn.positionY"),
                    (double) getConfig().getInt("map.spawn.positionZ"),
                    getConfig().getInt("map.spawn.rotation"), 0);
            player.teleport(location);
        }
        if (command.getName().equalsIgnoreCase("adminshop")) {
            Location location = new Location(player.getWorld(),
                    (double) getConfig().getInt("map.adminshop.positionX"),
                    (double) getConfig().getInt("map.adminshop.positionY"),
                    (double) getConfig().getInt("map.adminshop.positionZ"),
                    getConfig().getInt("map.adminshop.rotation"), 0);
            player.teleport(location);
        }

        return false;
    }

    ArrayList<Player> playerRule = new ArrayList<Player>();
    ConcurrentHashMap<String, ArrayList<Long>> chatLastSent = new ConcurrentHashMap<String, ArrayList<Long>>();
    ConcurrentHashMap<String, Boolean> actionTaken = new ConcurrentHashMap<String, Boolean>();

    @EventHandler
    public void onChat(final AsyncPlayerChatEvent event) {
        long timeInMillis = System.currentTimeMillis();
        int maxTM = getConfig().getInt("antispam") * 1000;
        int maxMSG = 5;
        if (chatLastSent.containsKey(event.getPlayer().getName())) {
            ArrayList<Long> g = new ArrayList<Long>();
            int tmpderp = 1;
            for (Long tmp : chatLastSent.get(event.getPlayer().getName())) {
                if ((maxTM + tmp) > timeInMillis) {
                    tmpderp++;
                    g.add(tmp);
                }
            }
            if (tmpderp >= maxMSG) {
                if (!getAction(event.getPlayer().getName())) {
                    if (!event.isAsynchronous()) {
                        event.getPlayer().sendMessage(ChatColor.RED + "Нефлуди! isAsynchronous()");
                    } else {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                event.getPlayer().sendMessage(ChatColor.RED + "Нефлуди! run()");
                            }
                        });
                    }
                    setAction(event.getPlayer().getName(), true);
                }
                event.setCancelled(true);
            }
            g.add(timeInMillis);
            chatLastSent.put(event.getPlayer().getName(), g);
        } else {
            ArrayList<Long> g = new ArrayList<Long>();
            g.add(timeInMillis);
            chatLastSent.put(event.getPlayer().getName(), g);
        }

        for (String str : mat) {
            if (event.getMessage().contains(str)) {
                if (playerRule.contains(event.getPlayer())) {
                    playerRule.remove(event.getPlayer());
                    event.getPlayer().sendMessage(ChatColor.RED + getConfig().getString("lang.matmute"));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute " + event.getPlayer().getName() + " 12h");
                    break;
                } else {
                    playerRule.add(event.getPlayer());
                    event.getPlayer().sendMessage(ChatColor.RED + getConfig().getString("lang.matrule"));
                    break;
                }

            }
        }
        for (String str : mat) {
            if (event.getMessage().contains(str)) {
                event.setMessage(event.getMessage().replace(str, "***"));
            }
        }
    }

    public boolean getAction(String player) {
        if (this.actionTaken.containsKey(player)) {
            return this.actionTaken.get(player);
        }
        return false;
    }

    public void setAction(String player, Boolean set) {
        this.actionTaken.put(player, set);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
//        Player player = (Player) event.getWhoClicked();
//        ItemStack item = event.getCurrentItem();
//        player.sendMessage(player.getName() + " крафт " + item);
//
//        if (item.getType().getId() == getConfig().getInt("craftintg.block")) {
//            player.sendMessage("Много крафтить нельзя!");
//        }
//
//        player.sendMessage("Все ок крафти дальше!");
    }

    public void loadPlugin() {

        Bukkit.getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);

        buildConfig();
        connectMysql();

    }

    public void reloadPlugin() {
        reloadConfig();
        connectMysql();
        mat = getConfig().getStringList("chat");
    }

    public void buildConfig() {

        saveConfig();
        reloadConfig();
        mat = getConfig().getStringList("chat");
    }

    public void log(String str) {
        //System.out.println(str);
        getServer().broadcastMessage(str);
    }

    public void connectMysql() {

        log("Connected to mysql");
        try {

            connection = DriverManager.getConnection(
                    "jdbc:mysql://"
                    + getConfig().getString("database.host") + ":"
                    + getConfig().getString("database.port") + "/"
                    + getConfig().getString("database.dbname"),
                    getConfig().getString("database.user"),
                    getConfig().getString("database.pass"));

            statement = connection.createStatement();
            log("MySQL OK");

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        log("On Loaded");
    }

    /**
     * Работ с файлом домов
     */
    FileConfiguration homes;

    /**
     * Возвращает конфиг домов
     *
     * @return
     */
    FileConfiguration getHomes() {
        return homes;
    }

    /**
     * Загрузка конфига домов
     */
    void loadHomes() {
        File home = new File(getDataFolder(), "home.yml");
        if (!home.exists()) {
            try {
                home.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        homes = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "home.yml"));

        try {
            homes.load(new File(getDataFolder(), "home.yml"));
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Перезагрузка конфига домов
     */
    void reloadHomes() {
        try {
            homes.load(new File(getDataFolder(), "home.yml"));
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Сохранение конфига домов
     */
    void saveHomes() {
        try {
            homes.save(new File(getDataFolder(), "home.yml"));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
