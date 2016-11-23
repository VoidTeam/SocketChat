package net.voidteam.socketchat;

import java.io.File;
import java.util.Collection;
import net.voidteam.socketchat.commands.SocketChatCommands;
import net.voidteam.socketchat.events.CommandEvents;
import net.voidteam.socketchat.events.MessageEvents;
import net.voidteam.socketchat.network.SocketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.WebSocket;


public class SocketChat extends JavaPlugin
{
    private SocketListener listener = null;     // SocketListener instance.
    public static int socketPort = 0;           // Port to listen on.
    public static String apiURL = "";           // API URL to validate SSO Tickets.
    public static String publicKey = "";        // Public Key for read only instances.
    public static boolean debugMode = false;    // Whether or not Debug Mode is enabled.

    /**
     * Plugin onDisable event.
     */
    @Override
    public void onDisable()
    {
        SocketChat.kickall("Server is restarting...");
        
        super.onDisable();

        SocketListener.activeSessions.clear();

        try {
            listener.stop();
        } catch (Exception e) {
            Utilities.severe("Could not close the WebSocket Listener!");
            e.printStackTrace();
        }
    }

    /**
     * Plugin onEnable event.
     */
    @Override
    public void onEnable()
    {
        super.onEnable();

        // Create default config if it does not exist yet.
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        // Load configuration.
        reloadConfiguration();
        
        // Create the SocketListener object.
        listener = new SocketListener(socketPort);
        listener.start();

        // Register the message event listener so that the WebChat can receive messages.
        Bukkit.getPluginManager().registerEvents(new MessageEvents(), this);
        Bukkit.getPluginManager().registerEvents(new CommandEvents(), this);

        // Register commands.
        getCommand("socketchat").setExecutor(new SocketChatCommands());
    }

    /**
     * Reloads the config file.
     */
    private void reloadConfiguration()
    {
        // Create default config if it does not exist yet.
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        // Reload config.
        reloadConfig();

        // Reassign variables from the config.
        socketPort = getConfig().getInt("socketPort");
        apiURL = getConfig().getString("apiURL");
        publicKey = getConfig().getString("publicKey");
        debugMode = Boolean.valueOf(getConfig().getString("debugMode"));
    }

    /**
     * Kick the specified player from WebChat.
     *
     * @param user Player to kick.
     * @param reason Reason the player was kicked.
     * @return boolean
     */
    public static boolean kick(String user, String reason)
    {
        String username;
        int x = 0;

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

        return x > 0;
    }

    /**
     * Kick all players from WebChat.
     *
     * @param reason Reason to kick all players.
     */
    public static void kickall(String reason)
    {
        SocketListener.activeSessions.keySet().stream().filter(WebSocket::isOpen).forEach(webSocket -> {
            if (reason != "") {
                webSocket.send("chat.kicked=" + reason);
            } else {
                webSocket.send("chat.kicked=" + reason);
            }

            webSocket.close();
        });
    }

    /**
     * Quick and easy way to return the SocketChat plugin instance.
     *
     * @return SocketChat
     */
    public static Plugin getPlugin()
    {
        return Bukkit.getPluginManager().getPlugin("SocketChat");
    }

    /**
     * Easy way to return a collection of the current WebChat users.
     *
     * @return Collection<String>
     */
    @SuppressWarnings("static-access")
    public Collection<String> getWebChatters()
    {
        return this.listener.activeSessions.values();
    }
}
