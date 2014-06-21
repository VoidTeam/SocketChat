package net.voidteam.socketchat.events;

import net.ess3.api.IEssentials;
import net.voidteam.socketchat.SocketChat;
import net.voidteam.socketchat.network.SocketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 12:38 PM
 */
public class MessageEvents implements Listener {
    public static List<String> cachedMessages = new ArrayList<String>();

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        final String formattedMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());

        /**
         * Add the message to the message cache
         */
        cachedMessages.add(0, formattedMessage);

        /**
         * If the size of the arraylist meets or exceeds 50 messages,
         * sublist it so we don't rape the webchat use with messages.
         */
        if (cachedMessages.size() >= 50)
            cachedMessages = cachedMessages.subList(0, 49);

        /**
         * Broadcast the message to the WebChat users.
         */

        boolean isMuted = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(event.getPlayer().getName()).isMuted();

        if (!isMuted) {
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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final String username = event.getPlayer().getName();
        boolean isHidden = false;
        
        /**
         * Send the player the message cache.
         */
        for (int i = cachedMessages.size() - 1; i >= 0; i--) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', cachedMessages.get(i)));
        }

        /**
         * If this player is not vanished,
         * Broadcast the join to webchat.
         */
        
        try {
        	isHidden = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(username).isHidden();
        }
        catch (NullPointerException ex) {}
        
        if (!isHidden) {
	        Bukkit.getScheduler().runTaskAsynchronously(SocketChat.getPlugin(), new Runnable() {
	            @Override
	            public void run() {
	                for (WebSocket socket : SocketListener.activeSessions.keySet()) {
	                    if (socket.isOpen()) {
	                        	socket.send(String.format("player.join=%s", username));
	                        	socket.send(String.format("online.list.join=%s", username));
	                    }
	                }
	            }
	        });
        }
        else
        {
	        Bukkit.getScheduler().runTaskAsynchronously(SocketChat.getPlugin(), new Runnable() {
	            @Override
	            public void run() {
	                for (WebSocket socket : SocketListener.activeSessions.keySet()) {
	                    if (socket.isOpen()) {
	                        	socket.send(String.format("player.join.vanished=%s", username));
	                    }
	                }
	            }
	        });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent event) {
        final String username = event.getPlayer().getName();
        boolean isHidden = false;

        /**
         * If this player is not vanished,
         * Broadcast the leave to webchat.
         */
        
        try {
        	isHidden = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(username).isHidden(); 
        }
        catch (NullPointerException ex) {}

        if (!isHidden) {
	        Bukkit.getScheduler().runTaskAsynchronously(SocketChat.getPlugin(), new Runnable() {
	            @Override
	            public void run() {
	                for (WebSocket socket : SocketListener.activeSessions.keySet()) {
	                    if (socket.isOpen()) {
	                        	socket.send(String.format("player.leave=%s", username));
	                        	socket.send(String.format("online.list.leave=%s", username));
	                    }
	                }
	            }
	        });
        }
        else
        {
        	Bukkit.getScheduler().runTaskAsynchronously(SocketChat.getPlugin(), new Runnable() {
	            @Override
	            public void run() {
	                for (WebSocket socket : SocketListener.activeSessions.keySet()) {
	                    if (socket.isOpen()) {
	                        	socket.send(String.format("player.leave.vanished=%s", username));
	                    }
	                }
	            }
	        });
        }
    }
}
