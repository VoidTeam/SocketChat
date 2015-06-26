/*
 * @date   6/26/15
 * @author Robert Duke
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this source code package.
 */
package net.voidteam.socketchat.events;

import net.voidteam.socketchat.SocketChat;
import net.voidteam.socketchat.network.SocketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.java_websocket.WebSocket;

import java.util.HashMap;

public class CommandEvents implements Listener {
    public static final HashMap<String, String> pmList = new HashMap<String, String>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void commandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] commandParts = event.getMessage().split(" ");

        if (commandParts[0].equalsIgnoreCase("/msg") ||
                commandParts[0].equalsIgnoreCase("/w") ||
                commandParts[0].equalsIgnoreCase("/m") ||
                commandParts[0].equalsIgnoreCase("/t") ||
                commandParts[0].equalsIgnoreCase("/emsg") ||
                commandParts[0].equalsIgnoreCase("/tell") ||
                commandParts[0].equalsIgnoreCase("/etell") ||
                commandParts[0].equalsIgnoreCase("/whisper") ||
                commandParts[0].equalsIgnoreCase("/ewhisper")) {
            String to = commandParts[1];

            boolean shouldBreak = true;
            for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
                if (SocketListener.activeSessions.get(webSocket).equalsIgnoreCase(to)) {
                    to = SocketListener.activeSessions.get(webSocket);
                    shouldBreak = false;
                    break;
                }
            }

            if(shouldBreak) {
                return;
            }

            String[] bits = new String[commandParts.length - 2];
            for (int x = 2; x < commandParts.length; x++) {
                bits[x - 2] = commandParts[x];
            }

            StringBuilder msg = new StringBuilder();
            for (String s : bits) {
                msg.append(s + " ");
            }

            sendPMToWebChat(player.getName(), to, msg.toString());

            /**
             * Now so it won't send a player not online message, if they aren't online.
             */
            if (Bukkit.getServer().getPlayer(to) == null || !Bukkit.getServer().getPlayer(to).isOnline()) {
                String chatMessage = String.format("&7[me&7 -> %s&7] &r%s", to, msg.toString());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));
                event.setCancelled(true);
            }
        } else if (commandParts[0].equalsIgnoreCase("/er") ||
                commandParts[0].equalsIgnoreCase("/reply") ||
                commandParts[0].equalsIgnoreCase("/ereply") ||
                commandParts[0].equalsIgnoreCase("/r")) {

            boolean shouldBreak = true;

            String lookupName = null;
            for(String key : pmList.keySet()) {
                if(player.getName().equalsIgnoreCase(key)) {
                    shouldBreak = false;
                    lookupName = pmList.get(key);
                }
            }

            if(shouldBreak) {
                return;
            }

            for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
                if (SocketListener.activeSessions.get(webSocket).equalsIgnoreCase(lookupName)) {
                    shouldBreak = false;
                    break;
                }
            }

            if(shouldBreak) {
                return;
            }

            String[] bits = new String[commandParts.length - 1];
            for (int x = 1; x < commandParts.length; x++) {
                bits[x - 1] = commandParts[x];
            }

            StringBuilder msg = new StringBuilder();
            for (String s : bits) {
                msg.append(s + " ");
            }

            sendPMToWebChat(player.getName(), lookupName, msg.toString());

            /**
             * Now so it won't send a player not online message, if they aren't online.
             */

            if (lookupName != null && Bukkit.getServer().getPlayer(lookupName) == null || !Bukkit.getServer().getPlayer(lookupName).isOnline()) {
                String chatMessage = String.format("&7[me&7 -> %s&7] &r%s", lookupName, msg.toString());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));
                event.setCancelled(true);
            }
        }

    }

    public static void sendPMToWebChat(String from, String to, String message) {
        /**
         * Check if the webchat session list contains that player.
         */
        if (((SocketChat) SocketChat.getPlugin()).getWebChatters().contains(to)) {
            /**
             * So replies work correctly.
             */
            if (pmList.containsKey(to)) {
                pmList.replace(to, from);
            } else {
                pmList.putIfAbsent(to, from);
            }

            if (pmList.containsKey(from)) {
                pmList.replace(from, to);
            } else {
                pmList.putIfAbsent(from, to);
            }

            SocketListener.sendMessage(to, String.format("chat.receive=&7[%s &7-> me] &r%s", from, message));
        }
    }
}
