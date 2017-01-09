package ru.enfester.plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.dispatchCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author antiv
 */
public class Main extends JavaPlugin implements Listener {

    Connection connection;
    Statement statement;
    ResultSet res;
    List<String> mat;

    FileConfiguration config;

    @Override
    public void onEnable() {

        loadPlugin();

    }

    @Override
    public void onDisable() {
        getServer().broadcastMessage("Мой плагин отключен");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            // игрох захидит на сервер 

            res = statement.executeQuery("SELECT * FROM `enefster_plugin_session` WHERE `name`='" + event.getPlayer().getName() + "';");
            if (!res.next()) { // Если нет в таблице то создаем 
                statement.executeUpdate("INSERT INTO `enefster_plugin_session` (`name`, `time_connect`, `time_disconnect`, `time_play`)"
                        + " VALUES ('" + event.getPlayer().getName() + "', CURRENT_TIMESTAMP, NULL, '0');");

                //event.getPlayer().getName()
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("enfester")) {
            if ("reload".equals(args[0])) {
                loadPlugin();
                return true;
            }
            if ("help".equals(args[0])) {

                return true;
            }
            if ("rtp".equals(args[0])) {
                new RandomTeleport(sender);
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
            }
            return true;
        }
        return true;
    }

    ArrayList<Player> playerRule = new ArrayList<Player>();

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        for (String str : mat) {
            if (event.getMessage().contains(str)) {
                if (playerRule.contains(event.getPlayer())) {
                    playerRule.remove(event.getPlayer());
                    event.getPlayer().sendMessage(ChatColor.RED + getConfig().getString("lang.matmute"));
                    dispatchCommand(Bukkit.getConsoleSender(), "mute " + event.getPlayer().getName() + " 12h");
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

}
