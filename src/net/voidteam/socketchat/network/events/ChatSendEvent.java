package net.voidteam.socketchat.network.events;

import net.ess3.api.IEssentials;
import net.voidteam.socketchat.OfflinePlayerLoader;
import net.voidteam.socketchat.SocketChat;
import net.voidteam.socketchat.Utilities;
import net.voidteam.socketchat.events.CommandEvents;
import net.voidteam.socketchat.network.SocketListener;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.java_websocket.WebSocket;

import java.util.HashSet;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 11:14 AM
 */
public class ChatSendEvent extends iEvent {
    public ChatSendEvent(WebSocket client, String payload) {
        super(client, payload);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        /**
         * Check if the client is actually verified.
         */
        if (!SocketListener.activeSessions.containsKey(getClient()))
            throw new IllegalArgumentException("sso.bad");

        /**
         * Check if the message is empty.
         */
        if (getPayload().isEmpty())
            throw new IllegalArgumentException("message.empty");

        String username = SocketListener.activeSessions.get(getClient());
        final String message = getPayload();

        boolean isMuted = false;
        try {
            isMuted = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(username).isMuted();
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("needs.profile");
        }

        if (isMuted) {
            throw new IllegalArgumentException("player.muted");
        }

        if (Bukkit.getBanList(BanList.Type.NAME).isBanned(username)) {
            throw new IllegalArgumentException("player.banned");
        }

        String[] commandParts = message.split(" ");

        /**
         * Sending a private message.
         */
        if (commandParts[0].equalsIgnoreCase("/msg") || commandParts[0].equalsIgnoreCase("/w") || commandParts[0].equalsIgnoreCase("/m") || commandParts[0].equalsIgnoreCase("/t") || commandParts[0].equalsIgnoreCase("/emsg") || commandParts[0].equalsIgnoreCase("/tell") || commandParts[0].equalsIgnoreCase("/etell") || commandParts[0].equalsIgnoreCase("/whisper") || commandParts[0].equalsIgnoreCase("/ewhisper")) {
            String to = commandParts[1];

            String[] bits = new String[commandParts.length - 2];
            for (int x = 2; x < commandParts.length; x++) {
                bits[x - 2] = commandParts[x];
            }

            StringBuilder msg = new StringBuilder();
            for (String s : bits) {
                msg.append(s + " ");
            }

            /**
             * Check if they are in-webchat.
             */
            for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
                if (SocketListener.activeSessions.get(webSocket).equalsIgnoreCase(to)) {
                    to = SocketListener.activeSessions.get(webSocket);
                    CommandEvents.sendPMToWebChat(username, to, msg.toString());
                    break;
                }
            }

            /**
             * Check if they are in-game.
             */
            if (Bukkit.getServer().getPlayer(to) != null && Bukkit.getServer().getPlayer(to).isOnline()) {
                /**
                 * Now so it won't send a player not online message, if they aren't online.
                 */
                String chatMessage = String.format("&7[%s &7-> me] &r%s", username, msg.toString());
                Bukkit.getServer().getPlayer(to).sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));

                /**
                 * So replies work correctly.
                 */
                if (CommandEvents.pmList.containsKey(to)) {
                    CommandEvents.pmList.replace(to, username);
                } else {
                    CommandEvents.pmList.put(to, username);
                }

                if (CommandEvents.pmList.containsKey(username)) {
                    CommandEvents.pmList.replace(username, to);
                } else {
                    CommandEvents.pmList.put(username, to);
                }
            }

            return;
        }

        /**
         * Replying to a private-message.
         */
        if (commandParts[0].equalsIgnoreCase("/er") || commandParts[0].equalsIgnoreCase("/reply") || commandParts[0].equalsIgnoreCase("/ereply") || commandParts[0].equalsIgnoreCase("/r")) {
            if (!CommandEvents.pmList.containsKey(username)) {
                return;
            }

            String to = CommandEvents.pmList.get(username);

            String[] bits = new String[commandParts.length - 1];
            for (int x = 1; x < commandParts.length; x++) {
                bits[x - 1] = commandParts[x];
            }

            StringBuilder msg = new StringBuilder();
            for (String s : bits) {
                msg.append(s + " ");
            }

            /**
             * Check if they are in-webchat.
             */
            for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
                if (SocketListener.activeSessions.get(webSocket).equalsIgnoreCase(to)) {
                    CommandEvents.sendPMToWebChat(username, to, msg.toString());
                    break;
                }
            }

            /**
             * Check if they are in-game.
             */
            if (Bukkit.getServer().getPlayer(to) != null && Bukkit.getServer().getPlayer(to).isOnline()) {
                /**
                 * Now so it won't send a player not online message, if they aren't online.
                 */
                String chatMessage = String.format("&7[%s &7-> me] &r%s", username, msg.toString());
                Bukkit.getServer().getPlayer(to).sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));

                /**
                 * So replies work correctly.
                 */
                if (CommandEvents.pmList.containsKey(to)) {
                    CommandEvents.pmList.replace(to, username);
                } else {
                    CommandEvents.pmList.putIfAbsent(to, username);
                }

                if (CommandEvents.pmList.containsKey(username)) {
                    CommandEvents.pmList.replace(username, to);
                } else {
                    CommandEvents.pmList.putIfAbsent(username, to);
                }
            }

            return;
        }

        if (Bukkit.getServer().getOnlinePlayers().size() > 0) {
            try {
                Player player = Bukkit.getServer().getPlayerExact(username);
                if (player == null)
                    player = OfflinePlayerLoader.load(username);

                AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, message, new HashSet<Player>(Bukkit.getServer().getOnlinePlayers()));
                Bukkit.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    Utilities.severe(String.format("Event cancelled while sending! [%s] [msg=%s]", getClient().getRemoteSocketAddress(), getPayload()));
                    throw new IllegalArgumentException("message.cancelled");
                }

                String formattedMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());

                //Find URLs and fix them
                if (message.contains("http://") || message.contains("https://")) {
                    String[] words = message.split(" ");
                    for (int x = 0; x < words.length; x++) {
                        if (words[x].contains(".") && (words[x].startsWith("http://") || words[x].startsWith("https://"))) //Ensure that the link is a link
                        {
                            String linkmessage = words[x];
                            formattedMessage = formattedMessage.replace(linkmessage.replace(".", " "), linkmessage);
                        }
                    }
                }

                // Send to players on server
                for (Player recipient : event.getRecipients())
                    recipient.sendMessage(formattedMessage);

                // Show in Console
                Bukkit.getServer().getConsoleSender().sendMessage(formattedMessage);

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalArgumentException("message.cancelled");
            }
        } else {
            /**
             * Broadcast the message to the WebChat users if no one is online
             */

            final String formattedMessage = "&7[Webchat] &e" + username + "&7: &f" + message.replaceAll("\\[[15]-[14][0]?\\]<.+> ", "");


            Bukkit.getScheduler().runTaskAsynchronously(SocketChat.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    for (WebSocket socket : SocketListener.activeSessions.keySet()) {
                        if (socket.isOpen()) {
                            socket.send(String.format("chat.receive=%s", formattedMessage.replaceAll("§", "&")));
                        }
                    }
                }
            });
        }
    }
}
