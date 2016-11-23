package net.voidteam.socketchat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class Utilities
{
    private static final String prefix = "";

    /**
     * Display any debuggable information to the console.
     *
     * @param debugMessage Message to be displayed to the console.
     */
    public static void debug(String debugMessage)
    {
        if (SocketChat.debugMode) {
            SocketChat.getPlugin().getLogger().log(Level.INFO, prefix.concat(debugMessage));
        }
    }

    /**
     * Display any runtime information to the console.
     *
     * @param logMessage Message to be displayed to the console.
     */
    public static void log(String logMessage)
    {
        SocketChat.getPlugin().getLogger().log(Level.INFO, prefix.concat(logMessage));
    }

    /**
     * Display any runtime errors to the console.
     *
     * @param severeMessage Message to be displayed to the console.
     */
    public static void severe(String severeMessage)
    {
        SocketChat.getPlugin().getLogger().log(Level.SEVERE, prefix.concat(severeMessage));
    }

    /**
     * Display any runtime warnings to the console.
     *
     * @param severeMessage Message to be displayed to the console.
     */
    public static void warning(String severeMessage)
    {
        SocketChat.getPlugin().getLogger().log(Level.WARNING, prefix.concat(severeMessage));
    }

    /**
     * Implode an array to a string.
     *
     * @param list List of strings to combine.
     * @return String
     */
    public static String implode(List<String> list)
    {

        // If this list is empty, it only contained blank values
        if (list.isEmpty()) {
            return "";
        }

        // Format the ArrayList as a string, similar to implode
        StringBuilder builder = new StringBuilder();

        for (String s : list) {
            builder.append(", ");
            builder.append(s);
        }

        return builder.toString();
    }

    /**
     * Find player based on Username.
     *
     * @param playerName Player's username.
     * @return Player
     */
    public static Player findPlayer(String playerName)
    {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Player playerFound = null;
        int foundPlayers = 0;

        if (onlinePlayers.size() > 0) {
            for (Player op : onlinePlayers) {
                if (op != null) {
                    if (op.getName().toLowerCase().startsWith(playerName.toLowerCase())) {
                        foundPlayers++;
                        playerFound = op;
                    }
                }
            }

            if (foundPlayers != 1) {
                playerFound = null;
            }
        }

        return playerFound;
    }
}
