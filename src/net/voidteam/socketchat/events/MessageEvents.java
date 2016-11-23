package net.voidteam.socketchat.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import net.ess3.api.IEssentials;
import net.voidteam.socketchat.JoinLeavePackets;
import net.voidteam.socketchat.SocketChat;
import net.voidteam.socketchat.Utilities;
import net.voidteam.socketchat.network.SocketListener;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.java_websocket.WebSocket;

public class MessageEvents implements Listener
{
    public static List<String> cachedMessages = Collections.synchronizedList(new ArrayList<String>());

    /**
     * Send (almost) all chat messages to WebChat.
     *
     * @param event AsyncPlayerChatEvent
     */
    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event)
    {
        final String formattedMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
        
        try
        {
            // Add the message to the message cache.
            cachedMessages.add(0, formattedMessage);

            // If the size of the ArrayList meets or exceeds 50 messages, sublist it so we don't spam the WebChat use
            // with messages.
            if (cachedMessages.size() >= 50) {
                cachedMessages = cachedMessages.subList(0, 49);
            }
        }
        catch(ConcurrentModificationException e)
        {
            Utilities.warning("Concurrent chat messages received. Resetting cache...");
            cachedMessages = Collections.synchronizedList(new ArrayList<String>());
        }

        // Broadcast the message to the WebChat users if they are not muted.
        boolean isMuted = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(event.getPlayer().getName()).isMuted();

        if (!isMuted) {
            Bukkit.getScheduler().runTaskAsynchronously(SocketChat.getPlugin(), () -> {
                SocketListener.activeSessions.keySet().stream().filter(WebSocket::isOpen).forEach(socket -> {
                    socket.send(String.format("chat.receive=%s", formattedMessage.replaceAll("§", "&")));
                });
            });
        }
    }

    /**
     * Send specific broadcasted chat messages to WebChat.
     *
     * @param event PlayerCommandPreprocessEvent
     */
    @EventHandler
    public void onPlayerBroadcastCommand(PlayerCommandPreprocessEvent event)
    {
        String print = null;
        String command = event.getMessage();
        String[] commands = command.split(" ");

        if (commands[0].equalsIgnoreCase("/say")) {
            commands[0] = "";
            String message = StringUtils.join(commands, " ");

            print = ChatColor.translateAlternateColorCodes('&', "&d[Server]") + message;
        } else if (commands[0].equalsIgnoreCase("/broadcast") || commands[0].equalsIgnoreCase("bc")) {
            commands[0] = "";
            String message = StringUtils.join(commands, " ");

            print = ChatColor.translateAlternateColorCodes('&', "&7[&4Broadcast&7]&a" + message);
        } else if (commands[0].equalsIgnoreCase("/slap")) {
            Player player = Utilities.findPlayer(commands[1]);

            print = ChatColor.translateAlternateColorCodes('&', event.getPlayer().getDisplayName() + " &eslapped " + player.getDisplayName());
        }

        if (print != null) {
            for (WebSocket socket : SocketListener.activeSessions.keySet()) {
                if (socket.isOpen()) {
                    socket.send(String.format("print.message=%s", print.replaceAll("§", "&")));
                }
            }
        }
    }

    /**
     * Send specific console broadcasted chat messages to WebChat.
     *
     * @param event ServerCommandEvent
     */
    @EventHandler
    public void onConsoleBroadcastCommand(ServerCommandEvent event)
    {
        String print = null;
        String command = event.getCommand();
        String[] commands = command.split(" ");

        if (commands[0].equalsIgnoreCase("say")) {
            commands[0] = "";
            String message = StringUtils.join(commands, " ");

            print = ChatColor.translateAlternateColorCodes('&', "&d[Server]") + message;
        } else if (commands[0].equalsIgnoreCase("broadcast") || commands[0].equalsIgnoreCase("bc")) {
            commands[0] = "";
            String message = StringUtils.join(commands, " ");

            print = ChatColor.translateAlternateColorCodes('&', "&7[&4Broadcast&7]&a" + message);
        } else if (commands[0].equalsIgnoreCase("slap")) {
            Player player = Utilities.findPlayer(commands[1]);

            print = ChatColor.translateAlternateColorCodes('&', "&6Console &eslapped " + player.getDisplayName());
        }

        if (print != null) {
            for (WebSocket socket : SocketListener.activeSessions.keySet()) {
                if (socket.isOpen()) {
                    socket.send(String.format("print.message=%s", print.replaceAll("§", "&")));
                }
            }
        }
    }

    /**
     * Show player join messages on WebChat.
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        final String username = event.getPlayer().getName();
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(SocketChat.getPlugin(), () -> JoinLeavePackets.joinServer(username), 20);
    }

    /**
     * Show player quit messages on WebChat.
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent event)
    {
        final String username = event.getPlayer().getName();
        
        JoinLeavePackets.leaveServer(username);
    }
}
