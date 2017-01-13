/*
 * Класс CommandExecutor
 * sethome сохранение точки дома в файл home.yml
 * home телепортация игрока к точки дома
 */
package ru.enfester.plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

    Main plugin;
    File homeFile;
    FileConfiguration homes;

    /**
     * Конструктор загрузки конфика домов Если нет файла home.yml то создает его
     *
     * @param main главный класс
     */
    public Home(Main main) {
        plugin = main;
        homeFile = new File(plugin.getDataFolder(), "home.yml"); // путь к файлу домов
        if (!homeFile.exists()) { // если такой файл отсутствует
            try {
                homeFile.createNewFile(); // создаем файл
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        homes = YamlConfiguration.loadConfiguration(homeFile); // Загружаем YAMl файла домов

        try {
            homes.load(homeFile); // Читаем данные с файла
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        Location location = player.getLocation();
        String world = player.getWorld().getName();

        if (command.getName().equalsIgnoreCase("sethome")) { // игрок пишет команду sethome
            // Создаем игрока в конфие
            // Позиции дома
            // Если нету
            homes.createSection("Home." + player.getName());
            homes.createSection("Home." + player.getName() + ".world");
            homes.createSection("Home." + player.getName() + ".x");
            homes.createSection("Home." + player.getName() + ".y");
            homes.createSection("Home." + player.getName() + ".z");
            homes.createSection("Home." + player.getName() + ".rotation");
            
            // получаем позиции где стоит игрок
            double x = location.getBlockX();
            double y = location.getBlockY();
            double z = location.getBlockZ();

            // Записываем позиции где стоит игрок как позиции дома
            homes.set("Home." + player.getName() + ".world", world);
            homes.set("Home." + player.getName() + ".x", x);
            homes.set("Home." + player.getName() + ".y", y);
            homes.set("Home." + player.getName() + ".z", z);
            homes.set("Home." + player.getName() + ".rotation", location.getYaw()); // куда игрок повернут
            
            // Сихраняем все в конфиг домов
            saveHomes();

            // Пишем игрок об успешном сохранении дома
            player.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("lang.sethome"));
        }
     
        if (command.getName().equalsIgnoreCase("home")) { // Игрок пишет команду home
            if (toHome(player)) { // если дом есть
                // пишем сообщение - приветствие дома
                player.sendMessage(ChatColor.GREEN + plugin.getConfig().getString("lang.home"));
            } else {
                // иначе пишем что нет дома
                player.sendMessage(ChatColor.RED + plugin.getConfig().getString("lang.nohome"));
            }
        }
        return false;

    }

    /**
     * Телепортация игрока к точке дома, если он есть
     *
     * @param player игрок
     * @return еть дом или нету
     */
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

    /**
     * Возвращает позицию дома игрока
     *
     * @param player игрок
     * @return
     */
    Location getHome(Player player) {
        String world = homes.getString("Home." + player.getName() + ".world");
        double x = homes.getDouble("Home." + player.getName() + ".x");
        double y = homes.getDouble("Home." + player.getName() + ".y");
        double z = homes.getDouble("Home." + player.getName() + ".z");
        int rotation = (int) homes.getDouble("Home." + player.getName() + ".rotation");
        return new Location(plugin.getServer().getWorld(world), x, y, z, rotation, 0);
    }

    /**
     * Перезагрузка конфига домов
     */
    void reloadHomes() {
        try {
            homes.load(homeFile);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Сохранение конфига домов
     */
    void saveHomes() {
        try {
            homes.save(homeFile);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
