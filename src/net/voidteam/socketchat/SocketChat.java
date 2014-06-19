package net.voidteam.socketchat;

import net.voidteam.socketchat.events.MessageEvents;
import net.voidteam.socketchat.network.SocketListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.WebSocket;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 12:44 AM
 */

public class SocketChat extends JavaPlugin {
    private SocketListener listener = null;

    @Override
    public void onDisable() {
        super.onDisable();

        for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
            if (webSocket.isOpen())
                webSocket.close();

        }

        try {
            listener.stop();
        } catch (Exception ex) {
            Utilities.severe("Could not close the WebSocket Listener!");
            ex.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        /**
         * Create the SocketListener object
         * on port 1337. TODO - Add config option.
         */
        listener = new SocketListener(1337);
        listener.start();

        /**
         * Register the message event listener
         * so that the WebChat can receive messages.
         */
        Bukkit.getPluginManager().registerEvents(new MessageEvents(), this);
    }

    /**
     * Quick and easy way to return the SocketChat plugin instance.
     *
     * @return SocketChat JavaPlugin instance
     */
    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("SocketChat");
    }
}
