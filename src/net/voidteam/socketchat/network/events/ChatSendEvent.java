package net.voidteam.socketchat.network.events;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import net.voidteam.socketchat.Utilities;
import net.voidteam.socketchat.network.SocketListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.java_websocket.WebSocket;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 11:14 AM
 */
public class ChatSendEvent extends iEvent {
    public ChatSendEvent(WebSocket client, String payload) {
        super(client, payload);
    }

    @Override
    public void run() {
        /**
         * Check if the client is actually verified.
         */
        if (!SocketListener.activeSessions.containsKey(getClient()))
            throw new IllegalArgumentException("Authorization failed");

        /**
         * Check if the message is empty.
         */
        if (getPayload().isEmpty())
            throw new IllegalArgumentException("Empty message");

        String username = SocketListener.activeSessions.get(getClient());

        if(Bukkit.getOnlinePlayers().length == 0) {
            throw new IllegalArgumentException("No Players Online");
        }

        try {
            Player player = Bukkit.getServer().getPlayerExact(username);

            if (player == null)
                player = JSONAPI.loadOfflinePlayer(username);

            String message = getPayload();

            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, message, new HashSet<Player>(Arrays.asList(Bukkit.getServer().getOnlinePlayers())));
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                Utilities.severe(String.format("Event cancelled while sending! [%s] [msg=%s]", getClient().getRemoteSocketAddress(), getPayload()));
                throw new IllegalArgumentException("Message cancelled");
            }

            message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());

            for (Player recipient : Bukkit.getServer().getOnlinePlayers())
                recipient.sendMessage(message);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("Message cancelled");
        }
    }
}
