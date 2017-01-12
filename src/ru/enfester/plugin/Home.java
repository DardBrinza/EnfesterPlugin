package ru.enfester.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Samar
 */
public class Home implements CommandExecutor {

    public Main plugin;

    public Home(Main main) {
        this.plugin = main;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        Location location = player.getLocation();
        String world = player.getWorld().getName();

        if (command.getName().equalsIgnoreCase("sethome")) {
            plugin.getHomes().createSection("Home." + player.getName());
            plugin.getHomes().createSection("Home." + player.getName() + ".world");
            plugin.getHomes().createSection("Home." + player.getName() + ".x");
            plugin.getHomes().createSection("Home." + player.getName() + ".y");
            plugin.getHomes().createSection("Home." + player.getName() + ".z");
            plugin.getHomes().createSection("Home." + player.getName() + ".rotation");
            plugin.saveHomes();

            double x = location.getBlockX();
            double y = location.getBlockY();
            double z = location.getBlockZ();

            plugin.getHomes().set("Home." + player.getName() + ".world", world);
            plugin.getHomes().set("Home." + player.getName() + ".x", x);
            plugin.getHomes().set("Home." + player.getName() + ".y", y);
            plugin.getHomes().set("Home." + player.getName() + ".z", z);
            plugin.getHomes().set("Home." + player.getName() + ".rotation",location.getYaw() );
            plugin.saveHomes();

            player.sendMessage(ChatColor.GREEN + "Точка дома установлена.");
        }

        if (command.getName().equalsIgnoreCase("home")) {
            if (toHome(player)) {
                player.sendMessage(ChatColor.GREEN + "Добро пожаловать домой!");
            } else {
                player.sendMessage(ChatColor.RED + "У тебя нет дома! Напиши в чат /sethome что бы установить точку дома.");
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
        String world = plugin.getHomes().getString("Home." + player.getName() + ".world");
        double x = plugin.getHomes().getDouble("Home." + player.getName() + ".x");
        double y = plugin.getHomes().getDouble("Home." + player.getName() + ".y");
        double z = plugin.getHomes().getDouble("Home." + player.getName() + ".z");
        int rotation = (int) plugin.getHomes().getDouble("Home." + player.getName() + ".rotation");
        return new Location(plugin.getServer().getWorld(world), x, y, z, rotation, 0);
    }

}
