package net.voidteam.socketchat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.voidteam.socketchat.events.MessageEvents;
import net.voidteam.socketchat.network.SocketListener;
import net.voidteam.socketchat.network.events.SSOAuthorizeEvent;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
    public static int socketPort = 0;
    public static String apiURL = "";
    public static String publicKey = "";
    public static boolean debugMode = false;

    @Override
    public void onDisable() {
        SocketChat.kickall("Server is restarting...");
        
        super.onDisable();

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

        // Create default config if it does not exist yet.
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        // Load configuration.
        reloadConfiguration();
        
        /**
         * Create the SocketListener object.
         */
        listener = new SocketListener(socketPort);

        /** SSL REMOVED
        // load up the key store
		String STORETYPE = "JKS";
		String KEYSTORE = "plugins/SocketChat/keystore.jks";
		String STOREPASSWORD = "socketchat";
		String KEYPASSWORD = "socketchat";
		
		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance(STORETYPE);
			File kf = new File(KEYSTORE);
			ks.load( new FileInputStream(kf), STOREPASSWORD.toCharArray());
			
			KeyManagerFactory kmf = null;
			kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYPASSWORD.toCharArray());
			
			TrustManagerFactory tmf = null;
			tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
			
			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			listener.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
		} catch (Exception e) {
			e.printStackTrace();
		}
        **/
        
        listener.start();

        /**
         * Register the message event listener
         * so that the WebChat can receive messages.
         */
        Bukkit.getPluginManager().registerEvents(new MessageEvents(), this);
    }

    public void reloadConfiguration() {
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        reloadConfig();

        socketPort = getConfig().getInt("socketPort");
        apiURL = getConfig().getString("apiURL");
        publicKey = getConfig().getString("publicKey");
        debugMode = Boolean.valueOf(getConfig().getString("debugMode"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (label.equals("wkickall")) {
            if (!sender.hasPermission("socketchat.kickall")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                return true;
            }
            SocketChat.kickall(StringUtils.join(args, " "));
            return true;
    	}
    	if (label.equals("wkick")) {
            if (!sender.hasPermission("socketchat.kick")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                return true;
            }
            if (args.length < 1) {
                return false;
            }
            if (args.length == 1) {
            	if (SocketChat.kick(args[0], "") == false) {
                    sender.sendMessage(ChatColor.RED + args[0] + " is not in webchat!");
            	}
            }
            if (args.length > 1) {
            	if (SocketChat.kick(args[0], StringUtils.join(args, " ", 1, args.length)) == false) {
                    sender.sendMessage(ChatColor.RED + args[0] + " is not in webchat!");
            	}
            }
            return true;
    	}

    	if (args.length == 0)
    	{
            sender.sendMessage(ChatColor.AQUA + "[SocketChat] Made by Robby Duke (a.k.a NoEff3x).");
            return true;
    	}

    	if (args.length == 1)
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
	            } else {
	            	return true;
	            }
    		}
    		if (args[0].equalsIgnoreCase("kickall"))
    		{
                if (!sender.hasPermission("socketchat.kickall")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                SocketChat.kickall("");
                return true;
    		}
    		if (args[0].equalsIgnoreCase("fj"))
    		{
                if (!sender.hasPermission("vanish.fakeannounce")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                JoinLeavePackets.joinServer(sender.getName());
                return true;
    		}
    		if (args[0].equalsIgnoreCase("fq"))
    		{
                if (!sender.hasPermission("vanish.fakeannounce")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                JoinLeavePackets.leaveServer(sender.getName());
                return true;
    		}
            return false;
    	}

    	if (args.length > 1)
    	{
    		if (args[0].equalsIgnoreCase("kick"))
    		{
                if (!sender.hasPermission("socketchat.kick")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                if (SocketChat.kick(args[1], StringUtils.join(args, " ", 2, args.length)) == false) {
                    sender.sendMessage(ChatColor.RED + args[0] + " is not in webchat!");
            	}
    		}
    		if (args[0].equalsIgnoreCase("kickall"))
    		{
                if (!sender.hasPermission("socketchat.kickall")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                    return true;
                }
                SocketChat.kickall(StringUtils.join(args, " ", 1, args.length));
    		}
            return true;
    	}

        return false;
    }

    public static boolean kick(String user, String reason) {
            String username = "";

            Integer x = 0;
            for (WebSocket socket : SocketListener.activeSessions.keySet()) {
                username = SocketListener.activeSessions.get(socket);

                if (username.equalsIgnoreCase(user)) {
                    if (socket.isOpen()) {
                        if (!reason.equals("")) {
	        	            socket.send("chat.kicked=" + reason);
	                        Bukkit.getServer().broadcastMessage(ChatColor.DARK_AQUA + username + ChatColor.GRAY + " was kicked from webchat for " + reason);
                        } else {
	        	            socket.send("chat.kicked=None");
	                        Bukkit.getServer().broadcastMessage(ChatColor.DARK_AQUA + username + ChatColor.GRAY + " was kicked from webchat");
                        }
	                    socket.close();
                    }

                    SocketListener.activeSessions.remove(socket);
                	JoinLeavePackets.leaveWebChat(username);

                    x++;
                }
            }

            if (x > 0) {
            	return true;
            } else {
            	return false;
            }
    }

    public static void kickall(String reason) {
    	for (WebSocket webSocket : SocketListener.activeSessions.keySet()) {
	        if (webSocket.isOpen()) {
                if (reason != "") {
                	webSocket.send("chat.kicked=" + reason);
                } else {
                	webSocket.send("chat.kicked=" + reason);
                }
	            webSocket.close();
	        }
	    }
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

    public static Player findPlayer(String playerName) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Player playerFound = null;
        int foundPlayers = 0;

        if (onlinePlayers.size() > 0) {
            for (Player op : onlinePlayers) {
                if (op != null) {
                    if (op.getName().toLowerCase().startsWith(playerName.toLowerCase())) {
                        foundPlayers++;
                        playerFound = op;
                    }
                }
            }

            if (foundPlayers != 1) {
                playerFound = null;
            }
        }
        return playerFound;
    }

    /**
     * Quick and easy way to return the SocketChat plugin instance.
     *
     * @return SocketChat JavaPlugin instance
     */
    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("SocketChat");
    }

    /**
     * Easy way to return a hashmap of the current webchat users.
     */
    @SuppressWarnings("static-access")
	public Collection<String> getWebChatters() {
        return this.listener.activeSessions.values();
    }
}
