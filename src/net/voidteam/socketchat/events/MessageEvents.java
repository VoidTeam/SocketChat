package net.voidteam.socketchat.events;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 12:38 PM
 */
public class MessageEvents implements Listener {
	public static List<String> cachedMessages = Collections.synchronizedList(new ArrayList<String>());
	boolean concurrentPrevention = false;

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        final String formattedMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());

        //if (concurrentPrevention == false)
        //{
        //	concurrentPrevention = true;
	        try
	        {
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
	        }
	        catch(ConcurrentModificationException e)
	        {
	            Utilities.warning("Concurrent chat messages received. Resetting cache...");
	            cachedMessages = Collections.synchronizedList(new ArrayList<String>());
	        }
	
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
	        
	    //	concurrentPrevention = false;
        //}
    }
    
    @EventHandler
    public void onPlayerBroadcastCommand(PlayerCommandPreprocessEvent e) {
    	String print = null;
    	String command = e.getMessage();
    	String[] commands = command.split(" ");
    	
    	if (commands[0].equalsIgnoreCase("/say"))
    	{
    		commands[0] = "";
    		String message = StringUtils.join(commands, " ");
    		
    		print = ChatColor.translateAlternateColorCodes('&', "&d[Server]") + message;
    	}
    	else if (commands[0].equalsIgnoreCase("/broadcast") || commands[0].equalsIgnoreCase("bc"))
    	{
    		commands[0] = "";
    		String message = StringUtils.join(commands, " ");
    		
    		print = ChatColor.translateAlternateColorCodes('&', "&7[&4Broadcast&7]&a" + message);
    	}
    	else if (commands[0].equalsIgnoreCase("/slap"))
    	{
    		Player player = SocketChat.findPlayer(commands[1]);
    		
    		print = ChatColor.translateAlternateColorCodes('&', e.getPlayer().getDisplayName() + " &eslapped " + player.getDisplayName());
    	}
    	
        if (print != null) {
	        for (WebSocket socket : SocketListener.activeSessions.keySet()) {
	            if (socket.isOpen()) {
	            	socket.send(String.format("print.message=%s", print.replaceAll("§", "&")));
	            }
	        }
        }
    }
    
    @EventHandler
    public void onConsoleBroadcastCommand(ServerCommandEvent e) {
    	String print = null;
    	String command = e.getCommand();
    	String[] commands = command.split(" ");
    	
    	if (commands[0].equalsIgnoreCase("say"))
    	{
    		commands[0] = "";
    		String message = StringUtils.join(commands, " ");
    		
    		print = ChatColor.translateAlternateColorCodes('&', "&d[Server]") + message;
    	}
    	else if (commands[0].equalsIgnoreCase("broadcast") || commands[0].equalsIgnoreCase("bc"))
    	{
    		commands[0] = "";
    		String message = StringUtils.join(commands, " ");
    		
    		print = ChatColor.translateAlternateColorCodes('&', "&7[&4Broadcast&7]&a" + message);
    	}
    	else if (commands[0].equalsIgnoreCase("slap"))
    	{
    		Player player = SocketChat.findPlayer(commands[1]);
    		
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

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final String username = event.getPlayer().getName();
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(SocketChat.getPlugin(), new Runnable() {
            @Override
            public void run() {
                JoinLeavePackets.joinServer(username);
            }
        }, 20);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent event) {
        final String username = event.getPlayer().getName();
        
        JoinLeavePackets.leaveServer(username);
    }
}
