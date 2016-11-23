package net.voidteam.socketchat.network.events;

import org.java_websocket.WebSocket;

public class ChatUNAFKEvent extends iEvent
{
    public ChatUNAFKEvent(WebSocket client, String payload)
    {
        super(client, payload);
    }

    /**
     * Display that the player is no longer AFK on WebChat.
     *
     * @throws IllegalArgumentException
     */
    @Override
    public void run() throws IllegalArgumentException
    {
        // TODO
    }
}
