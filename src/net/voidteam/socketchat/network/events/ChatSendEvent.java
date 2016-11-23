package net.voidteam.socketchat.network.events;

import java.util.HashSet;
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

public class ChatSendEvent extends iEvent
{
    public ChatSendEvent(WebSocket client, String payload)
    {
        super(client, payload);
    }

    /**
     * Player sends chat message.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void run()
    {
        // Check if the client is actually verified.
        if (!SocketListener.activeSessions.containsKey(getClient())) {
            throw new IllegalArgumentException("sso.bad");
        }

        // Check if message is empty.
        if (getPayload().isEmpty()) {
            throw new IllegalArgumentException("message.empty");
        }

        String username = SocketListener.activeSessions.get(getClient());
        final String message = getPayload();
        boolean isMuted;

        // Check if the player is muted.
        try {
            isMuted = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(username).isMuted();
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("needs.profile");
        }

        if (isMuted) {
            throw new IllegalArgumentException("player.muted");
        }

        // Check if the player is banned.
        if (Bukkit.getBanList(BanList.Type.NAME).isBanned(username)) {
            throw new IllegalArgumentException("player.banned");
        }

        String[] commandParts = message.split(" ");

        // Sending private messages.
        if (
            commandParts[0].equalsIgnoreCase("/msg") ||
            commandParts[0].equalsIgnoreCase("/w") ||
            commandParts[0].equalsIgnoreCase("/m") ||
            commandParts[0].equalsIgnoreCase("/t") ||
            commandParts[0].equalsIgnoreCase("/emsg") ||
            commandParts[0].equalsIgnoreCase("/tell") ||
            commandParts[0].equalsIgnoreCase("/etell") ||
            commandParts[0].equalsIgnoreCase("/whisper") ||
            commandParts[0].equalsIgnoreCase("/ewhisper")
        ) {
            String to = commandParts[1];

            String[] bits = new String[commandParts.length - 2];
            System.arraycopy(commandParts, 2, bits, 0, commandParts.length - 2);

            StringBuilder msg = new StringBuilder();
            for (String s : bits) {
                msg.append(s).append(" ");
            }

            // Check if the receiver is in WebChat.
            for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
                if (SocketListener.activeSessions.get(webSocket).equalsIgnoreCase(to) || SocketListener.activeSessions.get(webSocket).toLowerCase().startsWith(to.toLowerCase())) {
                    to = SocketListener.activeSessions.get(webSocket);
                    CommandEvents.sendPMToWebChat(username, to, msg.toString());
                    break;
                }
            }

            // Check if they're in-game.
            if (Bukkit.getServer().getPlayer(to) != null && Bukkit.getServer().getPlayer(to).isOnline()) {
                // So it won't send a player not online message, if they aren't online.
                String chatMessage = String.format("&7[%s &7-> me] &r%s", username, msg.toString());
                Bukkit.getServer().getPlayer(to).sendMessage(ChatColor.translateAlternateColorCodes('&', chatMessage));
            }

            // Send a confirmation back to the WebChat.
            // TODO

            return;
        }

        // Send a normal chat message.
        if (Bukkit.getServer().getOnlinePlayers().size() > 0) {
            try {
                Player player = Bukkit.getServer().getPlayerExact(username);

                if (player == null) {
                    player = OfflinePlayerLoader.load(username);
                }

                AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, message, new HashSet<>(Bukkit.getServer().getOnlinePlayers()));
                Bukkit.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    Utilities.severe(String.format("Event cancelled while sending! [%s] [msg=%s]", getClient().getRemoteSocketAddress(), getPayload()));
                    throw new IllegalArgumentException("message.cancelled");
                }

                String formattedMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());

                // Find URLs and fix them.
                if (message.contains("http://") || message.contains("https://")) {
                    String[] words = message.split(" ");

                    for (String word : words) {
                        if (word.contains(".") && (word.startsWith("http://") || word.startsWith("https://"))) //Ensure that the link is a link
                        {
                            formattedMessage = formattedMessage.replace(word.replace(".", " "), word);
                        }
                    }
                }

                // Send to players on server.
                for (Player recipient : event.getRecipients()) {
                    recipient.sendMessage(formattedMessage);
                }

                // Show in Console.
                Bukkit.getServer().getConsoleSender().sendMessage(formattedMessage);

            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("message.cancelled");
            }
        } else {
            // Broadcast the message to the WebChat users if no one is online.
            final String formattedMessage = "&7[Webchat] &e" + username + "&7: &f" + message.replaceAll("\\[[15]-[14][0]?\\]<.+> ", "");

            Bukkit.getScheduler().runTaskAsynchronously(SocketChat.getPlugin(), () -> {
                SocketListener.activeSessions.keySet().stream().filter(WebSocket::isOpen).forEach(socket -> {
                    socket.send(String.format("chat.receive=%s", formattedMessage.replaceAll("§", "&")));
                });
            });
        }
    }
}
