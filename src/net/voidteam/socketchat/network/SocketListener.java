package net.voidteam.socketchat.network;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.ess3.api.IEssentials;
import net.voidteam.socketchat.JoinLeavePackets;
import net.voidteam.socketchat.Utilities;
import net.voidteam.socketchat.events.MessageEvents;
import net.voidteam.socketchat.network.events.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class SocketListener extends WebSocketServer
{
    private final HashMap<String, Class<? extends iEvent>> validEvents = new HashMap<>();
    public static final Map<WebSocket, String> activeSessions = Collections.synchronizedMap(new HashMap<WebSocket, String>());

    /**
     * SocketListener constructor.
     * Register the WebSocket events here.
     *
     * @param port The port to listen on.
     */
    public SocketListener(Integer port)
    {
        super(new InetSocketAddress(port));

        validEvents.put("sso.authorize", SSOAuthorizeEvent.class);
        validEvents.put("chat.send", ChatSendEvent.class);
        validEvents.put("chat.afk", ChatAFKEvent.class);
        validEvents.put("chat.unafk", ChatUNAFKEvent.class);
        validEvents.put("chat.blur", ChatBlurEvent.class);
        validEvents.put("chat.focus", ChatFocusEvent.class);
    }

    /**
     * On Socket open.
     *
     * @param conn The incoming connection.
     * @param handshake The client handshake.
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        Utilities.debug(String.format("New Connection [%s]", conn.getRemoteSocketAddress()));

        if (conn.isOpen()) {
            // Send the player the message cache.
            for (int i = MessageEvents.cachedMessages.size() - 1; i >= 0; i--) {
                conn.send(String.format("chat.history=%s", MessageEvents.cachedMessages.get(i).replaceAll("§", "&")));
            }

            // Send the player the server online list.
            for (Player player : Bukkit.getOnlinePlayers()) {
                String username = player.getName();
                boolean isHidden;
                String displayName;

                try {
                    isHidden = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(username).isHidden();
                    displayName = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(username).getDisplayName().replaceAll("§", "&");
                }
                catch (NullPointerException ex) {
                    isHidden = false;
                    displayName = username;
                }

                if (!isHidden) {
                    conn.send(String.format("online.list.join=%s", displayName));
                } else {
                    conn.send(String.format("online.list.join.vanished=%s", displayName));
                }
            }

            // Send the player the WebChat online list.
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

    /**
     * On Socket close remove the player from the active sessions and send disconnect packet.
     *
     * @param conn The senders WebSocket connection.
     * @param code Closed connection code.
     * @param reason Reason the socket is closing.
     * @param remote Whether or not the remote host closed the connection.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        Utilities.debug(String.format("Closed Connection [%s] [code=%d] [reason=%s]", conn.getRemoteSocketAddress(), code, reason));

        if (!activeSessions.containsKey(conn)) {
            return;
        }

        final String username = activeSessions.get(conn);
        
        JoinLeavePackets.leaveWebChat(username);

        if (activeSessions.containsKey(conn))
            activeSessions.remove(conn);
    }

    /**
     * Any time a message over any of the established connections sends us a message.
     *
     * @param conn The senders WebSocket connection.
     * @param message The String representation of the message sent.
     */
    @Override
    public void onMessage(WebSocket conn, String message)
    {
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

        // Check if the message header is a valid event.
        if (validEvents.containsKey(header)) {
            // Construct the found class, if any error occurs the try-catch statement will correct the issue.
            try {
                Class<? extends iEvent> event = validEvents.get(header);
                Constructor<? extends iEvent> cons = event.getConstructor(WebSocket.class, String.class);
                iEvent object = cons.newInstance(conn, payload);

                // Call the method run in the constructed class.
                try {
                    object.run();
                } catch (IllegalArgumentException e) {
                    // Send back the error provided by the exception. Usually something like 'Invalid SSO Ticket'
                    Utilities.debug(String.format("Sending Error Message: [%s] to [%s]", e.getMessage(), conn.getRemoteSocketAddress()));
                    conn.send(e.getMessage());
                }
            } catch (Exception e) {
                Utilities.severe(String.format("Could not instantiate %s!", messageBits[0]));
                e.printStackTrace();
            }
        }

    }

    /**
     * On Socket error print to the console.
     *
     * @param conn The senders WebSocket connection.
     * @param e Exception object.
     */
    @Override
    public void onError(WebSocket conn, Exception e)
    {
        Utilities.severe(String.format("Error [%s]", conn.getRemoteSocketAddress()));
        e.printStackTrace();
    }

    /**
     * Send a message to registered session by username.
     *
     * @param username Username of the active session.
     */
    public static boolean sendMessage(String username, String payload)
    {
        for (WebSocket webSocket : activeSessions.keySet()) {
            if (activeSessions.get(webSocket).equalsIgnoreCase(username)) {
                if (webSocket.isOpen()) {
                    webSocket.send(payload);
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }
}
