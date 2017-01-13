package ru.enfester.plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Samar
 */
public class Chat implements CommandExecutor {

    Main plugin;
    FileConfiguration chat;
    File chatFile;

    public Chat(Main main) {
        plugin = main;

        chatFile = new File(plugin.getDataFolder(), "chatformat.yml");
        if (!chatFile.exists()) {
            try {
                chatFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        chat = YamlConfiguration.loadConfiguration(chatFile);

        try {
            chat.load(chatFile);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("setprefix") && player.hasPermission("enfester.admin")) {
            setPrefix(player, args[0]);
            return true;
        }
        if (command.getName().equalsIgnoreCase("setchatcolor") && player.hasPermission("enfester.admin")) {
            setChatColor(player, args[0]);
            return true;
        }
        if (command.getName().equalsIgnoreCase("setnamecolor") && player.hasPermission("enfester.admin")) {
            setNameColor(player, args[0]);
            return true;
        }
        return false;
    }

    public String getChatColor(Player player) {
        return chat.getString(player.getName() + ".chatcolor");
    }

    public String getPrefix(Player player) {
        return chat.getString(player.getName() + ".prefix");
    }

    public String getNameColor(Player player) {
        return chat.getString(player.getName() + ".namecolor");
    }

    public void setChatColor(Player player, String value) {
        addConfig(player.getName(), "chatcolor", value);
    }

    public void setPrefix(Player player, String value) {
        addConfig(player.getName(), "prefix", value);
    }

    public void setNameColor(Player player, String value) {
        addConfig(player.getName(), "namecolor", value);
    }

    public void addConfig(String player, String param, String value) {
        try {
            if (chat.isString(player)) {
                chat.createSection(player);
                chat.createSection(player + ".chatcolor");
                chat.createSection(player + ".prefix");
                chat.createSection(player + ".namecolor");
            }
            chat.set(player + "." + param, value);
            chat.save(chatFile);
        } catch (IOException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
