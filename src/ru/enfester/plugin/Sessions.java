/*
 * Класс отлавливает евеннты полключение и выход игрока с сервера
 * Записывает время в базу данных
 * Высчитывает общее время игры
 */
package ru.enfester.plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Samar
 */
public class Sessions implements Listener {

    Main plugin;

    Statement statement;

    Sessions(Main main) {
        plugin = main;

        connectMysql();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            event.setJoinMessage("");
            ResultSet res = statement.executeQuery("SELECT * FROM `enefster_plugin_session` WHERE `name`='" + event.getPlayer().getName() + "';");

            if (!res.next()) { // Если нет в таблице то создаем 
                statement.executeUpdate("INSERT INTO `enefster_plugin_session` (`name`, `time_connect`, `time_disconnect`, `time_play`)"
                        + " VALUES ('" + event.getPlayer().getName() + "', CURRENT_TIMESTAMP, NULL, '0');");

            }
            statement.executeUpdate("UPDATE `enefster_plugin_session` SET `time_connect`= CURRENT_TIMESTAMP WHERE `name`='" + event.getPlayer().getName() + "';");

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try {
            event.setQuitMessage("");
            statement.executeUpdate("UPDATE `enefster_plugin_session` SET `time_disconnect`= CURRENT_TIMESTAMP, `time_play` = (CURRENT_TIMESTAMP - `time_connect` + `time_play`) WHERE `name`='" + event.getPlayer().getName() + "';");

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

     void connectMysql() {

        try {
            Connection connection;
            connection = DriverManager.getConnection(
                    "jdbc:mysql://"
                    + plugin.getConfig().getString("database.host") + ":"
                    + plugin.getConfig().getString("database.port") + "/"
                    + plugin.getConfig().getString("database.dbname"),
                    plugin.getConfig().getString("database.user"),
                    plugin.getConfig().getString("database.pass"));

            statement = connection.createStatement();

        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
