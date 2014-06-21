package net.voidteam.socketchat;

import net.voidteam.socketchat.events.MessageEvents;
import net.voidteam.socketchat.network.SocketListener;
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
            SocketChat.kickall();
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
        	SocketChat.kick(args[0]);
            return true;
    	}
    	
    	if (args.length == 0)
    	{
            sender.sendMessage(ChatColor.AQUA + "[SocketChat] Made by Robby Duke (a.k.a NoEff3x).");
            return true;
    	}
    	
    	if (args.length == 1)
    	{
    		if (args[0].equalsIgnoreCase("kickall"))
    		{
                if (!sender.hasPermission("socketchat.kickall")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return false;
                }
                SocketChat.kickall();
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
    	
    	if (args.length == 2)
    	{
    		if (args[0].equalsIgnoreCase("kick"))
    		{
                if (!sender.hasPermission("socketchat.kick")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return false;
                }
                SocketChat.kick(args[1]);
    		}
            return true;
    	}

        return false;
    }

    public static boolean kick(String user) {
        if (SocketListener.activeSessions.containsValue(user)) {
            String username = "";

            for (WebSocket socket : SocketListener.activeSessions.keySet()) {
                username = SocketListener.activeSessions.get(socket);

                if (username.equalsIgnoreCase(user)) {
                    if (socket.isOpen()) {
                        socket.send("chat.kicked");
                        socket.close();
                        Bukkit.getServer().broadcastMessage(ChatColor.GOLD + username + " was kicked from webchat.");
                    }

                    SocketListener.activeSessions.remove(socket);
                    return true;
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

    public static boolean kickall() {
    	for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
	        if (webSocket.isOpen()) {
	            webSocket.send("chat.kicked");
	            webSocket.close();
	            return true;
	        }
	    }
		return false;
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
