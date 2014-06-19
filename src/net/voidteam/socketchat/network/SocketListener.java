package net.voidteam.socketchat.network;


import net.voidteam.socketchat.Utilities;
import net.voidteam.socketchat.network.events.ChatSendEvent;
import net.voidteam.socketchat.network.events.SSOAuthorizeEvent;
import net.voidteam.socketchat.network.events.iEvent;
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
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Utilities.debug(String.format("Closed Connection [%s] [code=%d] [reason=%s]", conn.getRemoteSocketAddress(), code, reason));

        if (activeSessions.containsKey(conn))
            activeSessions.remove(conn);
    }

    /**
     * Anytime a message over any of the established
     * connections sends us a message.
     *
     * @param conn    The senders WebSocket connection.
     * @param message The String representation of the message sent.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        Utilities.debug(String.format("Received [%s] from [%s]", message, conn.getRemoteSocketAddress()));
        String[] messageBits = message.split("=");

        /**
         * Check if the message header is a valid event.
         */
        if (validEvents.containsKey(messageBits[0])) {
            /**
             * Construct the found class, if any error occurs
             * the try-catch statement will correct the issue.
             */
            try {
                Class<? extends iEvent> event = validEvents.get(messageBits[0]);
                Constructor<? extends iEvent> cons = event.getConstructor(WebSocket.class, String.class);
                iEvent object = cons.newInstance(conn, messageBits[1]);

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
                    conn.send(ex.getMessage());
                    Utilities.debug(String.format("Bad argument for [%s] [msg=%s]", conn.getRemoteSocketAddress(), ex.getMessage()));
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
