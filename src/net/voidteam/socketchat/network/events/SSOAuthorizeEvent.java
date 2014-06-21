package net.voidteam.socketchat.network.events;

import net.ess3.api.IEssentials;
import net.ess3.api.IUser;
import net.minecraft.util.com.google.gson.internal.LinkedTreeMap;
import net.voidteam.socketchat.Utilities;
import net.voidteam.socketchat.network.SocketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 10:55 AM
 */
public class SSOAuthorizeEvent extends iEvent {
    /**
     * Default constructor for the iEvent class.
     *
     * @param payload The SSO ticket being authorized.
     */
    public SSOAuthorizeEvent(WebSocket client, String payload) {
        super(client, payload);
    }

    public static List<String> spyList = new ArrayList<String>();


    @SuppressWarnings({ "rawtypes", "deprecation" })
	@Override
    public void run() {
        String ticket = getPayload();
        boolean spy = false;
        
        if (SocketListener.activeSessions.containsKey(getClient())) {
            throw new IllegalArgumentException("already.authed");
        }
        
        if (ticket.contains("-spy-")) {
        	spy = true;
        	ticket = ticket.replaceAll("-spy-", "");
        }

        if (ticket.length() != 50) {
            throw new IllegalArgumentException("bad.sso=length."+ticket);
        }

        String json = null;

        /**
         * TODO - Add URL to config
         */
        try {
            json = getText("http://voidteam.net/minecraft/api/check_webchat_ticket/".concat(ticket));
        } catch (Exception ex) {
            Utilities.severe("Couldn't fetch JSON!");
            ex.printStackTrace();

            throw new IllegalArgumentException("network.unreachable");
        }

        /**
         * Create the GSON object and turn the JSON
         * into a LinkedTreeMap for ease of use.
         */
        Gson gson = new Gson();
        LinkedTreeMap jsonValues = gson.fromJson(json, LinkedTreeMap.class);

        if (jsonValues.containsKey("profile")) {
            throw new IllegalArgumentException("bad.sso=profile."+ticket);
        }

        String username = (String) jsonValues.get("username");
        Utilities.debug(String.format("Validated SSO Ticket [%s] [username=%s]", ticket, username));

        SocketListener.activeSessions.put(getClient(), username);

        if (getClient().isOpen())
            getClient().send("sso.validated");

        IUser iUser = null;
        try {
            iUser = ((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials")).getUser(username);
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("needs.profile");
        }

        if (iUser == null) {
            throw new IllegalArgumentException("needs.profile");
        }
        
        if (spy == true) {
            //Player player = Bukkit.getPlayer(username);
            // if (!player.hasPermission("socketchat.spy")) {
            //     throw new IllegalArgumentException("no.spy");
            // }
            spyList.add(username);
            getClient().send("spying");
        }
        
        for (WebSocket socket : SocketListener.activeSessions.keySet()) {
            if (socket.isOpen()) {
                if (!spyList.contains(username)) {
                	socket.send(String.format("player.webchat.join=%s", username));
                	socket.send(String.format("online.list.webchat.join=%s", username));
                } else {
                	socket.send(String.format("player.webchat.join.spy=%s", username));
                }
            }
        }

        if (!spyList.contains(username))
	        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + username + " joined the webchat.");
    }

    public static String getText(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        return response.toString();
    }
}
