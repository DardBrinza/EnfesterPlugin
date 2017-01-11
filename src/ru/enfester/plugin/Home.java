package ru.enfester.plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
public class Home implements CommandExecutor {

    public Main main;

    public Home(Main main) {
        this.main = main;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        Location location = player.getLocation();
        String world = player.getWorld().getName();

        if (command.getName().equalsIgnoreCase("sethome")) {
            main.getHomes().createSection("Home." + player.getName());
            main.getHomes().createSection("Home." + player.getName() + ".world");
            main.getHomes().createSection("Home." + player.getName() + ".x");
            main.getHomes().createSection("Home." + player.getName() + ".y");
            main.getHomes().createSection("Home." + player.getName() + ".y");
            main.saveHomes();

            double x = location.getBlockX();
            double y = location.getBlockY();
            double z = location.getBlockZ();

            main.getHomes().set("Home." + player.getName() + ".world", world);
            main.getHomes().set("Home." + player.getName() + ".x", x);
            main.getHomes().set("Home." + player.getName() + ".y", y);
            main.getHomes().set("Home." + player.getName() + ".z", z);
            main.saveHomes();

            player.sendMessage(ChatColor.GREEN + "Точка дома установлена.");
        }

        if (command.getName().equalsIgnoreCase("home")) {
            if (toHome(player)) {
                player.sendMessage(ChatColor.GREEN + "Добро пожаловать домой!");
            } else {
                player.sendMessage(ChatColor.DARK_RED + "У тебя нет дома! Напиши в чат /sethome что бы установить точку дома.");
            }
        }
        return false;

    }

    boolean toHome(Player player) {
        Location location = getHome(player);
        double x = location.getZ();
        double y = location.getY();
        double z = location.getZ();

        if (x != 0.0 && y != 0.0 && z != 0.0) {

            player.teleport(location);
            return true;

        } else {
            return false;
        }
    }

    Location getHome(Player player) {
        String world = main.getHomes().getString("Home." + player.getName() + ".world");
        double x = main.getHomes().getDouble("Home." + player.getName() + ".x");
        double y = main.getHomes().getDouble("Home." + player.getName() + ".y");
        double z = main.getHomes().getDouble("Home." + player.getName() + ".z");

        return new Location(main.getServer().getWorld(world), x, y, z);
    }

  

}
