package ru.enfester.plugin;

import static org.bukkit.Bukkit.getServer;
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
public class Spawn implements CommandExecutor {

    Main plugin;

    public Spawn(Main main) {
        plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        if (player.hasPermission("enfester.admin")) {
            if (command.getName().equalsIgnoreCase("setspawn")) {
                Location location = player.getLocation();
                plugin.getConfig().set("map.spawn.world", player.getWorld().getName());
                plugin.getConfig().set("map.spawn.positionX", location.getX());
                plugin.getConfig().set("map.spawn.positionY", location.getY());
                plugin.getConfig().set("map.spawn.positionZ", location.getZ());
                plugin.getConfig().set("map.spawn.rotation", location.getYaw());
                plugin.buildConfig();
                player.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("lang.setspawn"));
                return true;
            }
            if (command.getName().equalsIgnoreCase("setadminshop")) {
                Location location = player.getLocation();

                plugin.getConfig().set("map.adminshop.world", player.getWorld().getName());
                plugin.getConfig().set("map.adminshop.positionX", location.getX());
                plugin.getConfig().set("map.adminshop.positionY", location.getY());
                plugin.getConfig().set("map.adminshop.positionZ", location.getZ());
                plugin.getConfig().set("map.adminshop.rotation", location.getYaw());
                plugin.buildConfig();
                player.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("lang.setadminshop"));
                return true;
            }
        } else {
            player.sendMessage(ChatColor.RED + plugin.getConfig().getString("lang.permission"));
        }
        if (command.getName().equalsIgnoreCase("spawn")) {

            Location location = new Location(getServer().getWorld(plugin.getConfig().getString("map.spawn.world")),
                    (double) plugin.getConfig().getInt("map.spawn.positionX"),
                    (double) plugin.getConfig().getInt("map.spawn.positionY"),
                    (double) plugin.getConfig().getInt("map.spawn.positionZ"),
                    plugin.getConfig().getInt("map.spawn.rotation"), 0);
            player.teleport(location);
            return true;
        }
        if (command.getName().equalsIgnoreCase("adminshop")) {
            Location location = new Location(getServer().getWorld(plugin.getConfig().getString("map.adminshop.world")),
                    (double) plugin.getConfig().getInt("map.adminshop.positionX"),
                    (double) plugin.getConfig().getInt("map.adminshop.positionY"),
                    (double) plugin.getConfig().getInt("map.adminshop.positionZ"),
                    plugin.getConfig().getInt("map.adminshop.rotation"), 0);
            player.teleport(location);
            return true;
        }
        return false;
    }

}
