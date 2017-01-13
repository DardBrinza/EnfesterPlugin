package ru.enfester.plugin;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author Samar
 */
public class RandomTeleport {

    private HashMap<Player, Long> lastrtp = new HashMap<Player, Long>();

    public RandomTeleport(Player player) {
        if (lastrtp.containsKey(player)) {
            if (player.isOp()) {
                randomTP(player);
            }

            Timestamp last = new Timestamp(lastrtp.get(player) + (1 * 60 * 1000));
            Date date = new Date();
            Timestamp now = new Timestamp(date.getTime());

            if (now.after(last)) {
                randomTP(player);
            } else {
                player.sendMessage("Sorry, you must wait to randomly teleport again");
            }
        } else {
            randomTP(player);
        }
    }

    private void randomTP(Player player) {
        Date date = new Date();
        Random rand = new Random();

        if (lastrtp.containsKey(player)) {
            lastrtp.remove(player);
        }

        lastrtp.put(player, date.getTime());

        int max_coords = 4000;
        int randx = rand.nextInt(max_coords * 2) - max_coords;
        int randz = rand.nextInt(max_coords * 2) - max_coords;
        int randy = rand.nextInt(player.getWorld().getMaxHeight()) + 1;

        Block block = player.getWorld().getBlockAt(randx, randy, randz);

        if (block.getTypeId() != 0) {
            randy = player.getWorld().getHighestBlockYAt(randx, randz);
        }

        Location location = new Location(player.getWorld(), (double) randx, (double) randy, (double) randz);
        player.teleport(location);
    }
}
