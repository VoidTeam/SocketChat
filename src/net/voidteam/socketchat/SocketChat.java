package net.voidteam.socketchat;

import java.util.ArrayList;
import java.util.List;

import net.voidteam.socketchat.events.MessageEvents;
import net.voidteam.socketchat.network.SocketListener;
import net.voidteam.socketchat.network.events.SSOAuthorizeEvent;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

        SocketListener.activeSessions.clear();

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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (label.equals("wkickall")) {
            if (!sender.hasPermission("socketchat.kickall")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                return false;
            }
            SocketChat.kickall(StringUtils.join(args, " "));
            return true;
    	}   
    	if (label.equals("wkick")) {
            if (!sender.hasPermission("socketchat.kick")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                return false;
            }
            if (args.length < 1) {
                return false;
            }
        	SocketChat.kick(args[0], StringUtils.join(args, " ", 1, args.length-1));
            return true;
    	}
    	
    	if (args.length == 0)
    	{
            sender.sendMessage(ChatColor.AQUA + "[SocketChat] Made by Robby Duke (a.k.a NoEff3x).");
            return true;
    	}
    	
    	if (args.length > 1)
    	{
    		if (args[0].equalsIgnoreCase("kick"))
    		{
                if (!sender.hasPermission("socketchat.kick")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return false;
                }
                SocketChat.kick(args[1], StringUtils.join(args, " ", 2, args.length-1));
    		}
    		if (args[0].equalsIgnoreCase("kickall"))
    		{
                if (!sender.hasPermission("socketchat.kickall")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return false;
                }
                SocketChat.kickall(StringUtils.join(args, " ", 1, args.length-1));
    		}
            return true;
    	}
    	else if (args.length > 0)
    	{
    		if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("who"))
    		{
    		    List<String> onlineList = new ArrayList<String>();
    		    
	            for (String username : SocketListener.activeSessions.values()) {
	                if (SSOAuthorizeEvent.spyList.contains(username)) {
	                	if (sender.hasPermission("socketchat.spy")) {
	                		onlineList.add("&8[HIDDEN]&e" + username + "&f");
	                	}
	                } else {
		                onlineList.add("&e" + username + "&f");
	                }
	            }
	            if (!implode(onlineList).equals("")) {
	            	sender.sendMessage(ChatColor.GRAY + "Webchat: " + ChatColor.translateAlternateColorCodes('&', implode(onlineList).substring(2)));
	            	return true;
	            }
    		}
    		if (args[0].equalsIgnoreCase("kickall"))
    		{
                if (!sender.hasPermission("socketchat.kickall")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return false;
                }
                SocketChat.kickall("");
    		}
    		if (args[0].equalsIgnoreCase("fj"))
    		{
                if (!sender.hasPermission("vanish.fakeannounce")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return false;
                }
                for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
                	if (webSocket.isOpen()) {
                		webSocket.send(String.format("player.join=%s", sender.getName()));
                	}
                }
    		}
    		if (args[0].equalsIgnoreCase("fq"))
    		{
                if (!sender.hasPermission("vanish.fakeannounce")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return false;
                }
                for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
                	if (webSocket.isOpen()) {
                		webSocket.send(String.format("player.leave=%s", sender.getName()));
                	}
                }
    		}
            return true;
    	}

        return false;
    }

    public static boolean kick(String user, String reason) {
        if (SocketListener.activeSessions.containsValue(user)) {
            String username = "";

            for (WebSocket socket : SocketListener.activeSessions.keySet()) {
                username = SocketListener.activeSessions.get(socket);

                if (username.equalsIgnoreCase(user)) {
                    if (socket.isOpen()) {
        	            socket.send("chat.kicked=" + reason);
                        socket.close();
                        Bukkit.getServer().broadcastMessage(ChatColor.DARK_AQUA + username + ChatColor.GRAY + " was kicked from webchat for " + reason);
                    }

                    SocketListener.activeSessions.remove(socket);
                } else {
                    if (socket.isOpen()) {
                        socket.send(String.format("player.leave.webchat=%s", username));
                    }
                }
            }
            return true;
        }
		return false;
    }

    public static boolean kickall(String reason) {
    	for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
	        if (webSocket.isOpen()) {
	            webSocket.send("chat.kicked=" + reason);
	            webSocket.close();
	        }
	    }
        return true;
    }
    
    public static String implode(List<String> list) {
    	
        // If this list is empty, it only contained blank values
        if( list.isEmpty()) {
            return "";
        }

        // Format the ArrayList as a string, similar to implode
        StringBuilder builder = new StringBuilder();
        
        for( String s : list) {
            builder.append( ", ");
            builder.append( s);
        }

        return builder.toString();
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
