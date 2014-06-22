package net.voidteam.socketchat.network;


import net.ess3.api.IEssentials;
import net.voidteam.socketchat.Utilities;
import net.voidteam.socketchat.events.MessageEvents;
import net.voidteam.socketchat.network.events.ChatSendEvent;
import net.voidteam.socketchat.network.events.SSOAuthorizeEvent;
import net.voidteam.socketchat.network.events.iEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 10:52 AM
 */
public class SocketListener extends WebSocketServer {
    private final HashMap<String, Class<? extends iEvent>> validEvents = new HashMap<String, Class<? extends iEvent>>();
    public static final HashMap<WebSocket, String> activeSessions = new HashMap<WebSocket, String>();

    public SocketListener(Integer port) {
        super(new InetSocketAddress(port));

        /**
         * Register the WebSocket events here.
         */
        validEvents.put("sso.authorize", SSOAuthorizeEvent.class);
        validEvents.put("chat.send", ChatSendEvent.class);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Utilities.debug(String.format("New Connection [%s]", conn.getRemoteSocketAddress()));

        /**
         * Send the player the message cache.
         */
        if (conn.isOpen()) {
            for (int i = MessageEvents.cachedMessages.size() - 1; i >= 0; i--) {
                conn.send(String.format("chat.history=%s", MessageEvents.cachedMessages.get(i).replaceAll("§", "&")));
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                boolean isHidden = false;
                String username = player.getName();
                
                try {
                	isHidden = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(player.getName()).isHidden();
                }
                catch (NullPointerException ex) {}
            	
                if (isHidden == false)
                	conn.send(String.format("online.list.join=%s", username));
            }

            for (String username : activeSessions.values()) {
                
                if (!SSOAuthorizeEvent.spyList.contains(username)) {
	                conn.send(String.format("online.list.webchat.join=%s", username));
                }
                else {
	                conn.send(String.format("online.list.webchat.join.spy=%s", username));
                }
            }
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Utilities.debug(String.format("Closed Connection [%s] [code=%d] [reason=%s]", conn.getRemoteSocketAddress(), code, reason));


        if (!activeSessions.containsKey(conn)) {
            return;
        }

        final String username = activeSessions.get(conn);
        
        for (WebSocket socket : SocketListener.activeSessions.keySet()) {
            if (socket.isOpen()) {
                if (!SSOAuthorizeEvent.spyList.contains(username)) {
                	socket.send(String.format("player.webchat.leave=%s", username));
                	socket.send(String.format("online.list.webchat.leave=%s", username));
                } else {
                	socket.send(String.format("player.webchat.leave.spy=%s", username));
                	socket.send(String.format("online.list.webchat.leave.spy=%s", username));
                }
            }
        }

        if (!SSOAuthorizeEvent.spyList.contains(username)) {
        	Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + username + " left the webchat.");
        } else {
    		SSOAuthorizeEvent.spyList.remove(username);
        }

        if (activeSessions.containsKey(conn))
            activeSessions.remove(conn);
    }

    /**
     * Any time a message over any of the established
     * connections sends us a message.
     *
     * @param conn    The senders WebSocket connection.
     * @param message The String representation of the message sent.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        Utilities.debug(String.format("Received [%s] from [%s]", message, conn.getRemoteSocketAddress()));
        String[] messageBits = message.split("=", 2);

        String header = "";
        String payload = "";

        if(messageBits.length == 1) {
            header = messageBits[0];
        } else if(messageBits.length == 2) {
            header = messageBits[0];
            payload = messageBits[1];
        }

        /**
         * Check if the message header is a valid event.
         */
        if (validEvents.containsKey(header)) {
            /**
             * Construct the found class, if any error occurs
             * the try-catch statement will correct the issue.
             */
            try {
                Class<? extends iEvent> event = validEvents.get(header);
                Constructor<? extends iEvent> cons = event.getConstructor(WebSocket.class, String.class);
                iEvent object = cons.newInstance(conn, payload);

                /**
                 * Call the method run in the constructed class.
                 */
                try {
                    object.run();
                } catch (IllegalArgumentException ex) {
                    /**
                     * Send back the error provided by the exception.
                     * Usually something like 'Invalid SSO Ticket'
                     */
                    Utilities.debug(String.format("Sending Error Message: [%s] to [%s]", ex.getMessage(), conn.getRemoteSocketAddress()));
                    conn.send(ex.getMessage());
                }
            } catch (Exception ex) {
                Utilities.severe(String.format("Could not instantiate %s!", messageBits[0]));
                ex.printStackTrace();
            }
        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Utilities.severe(String.format("Error [%s]", conn.getRemoteSocketAddress()));
        ex.printStackTrace();
    }

    /**
     * Send a message to registered session by username.
     *
     * @param username Username of the active session.
     */
    public static boolean sendMessage(String username, String payload) {
        if (activeSessions.containsValue(username)) {
            for (WebSocket webSocket : activeSessions.keySet()) {
                if (activeSessions.get(webSocket).equals(username)) {
                    if (webSocket.isOpen()) {
                        webSocket.send(payload);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }

        return false;
    }
}
