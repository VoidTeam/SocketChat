package net.voidteam.socketchat.network.events;

import org.java_websocket.WebSocket;

public class ChatAFKEvent extends iEvent
{
    public ChatAFKEvent(WebSocket client, String payload)
    {
        super(client, payload);
    }

    /**
     * Display that the player has gone AFK on WebChat.
     *
     * @throws IllegalArgumentException
     */
    @Override
    public void run() throws IllegalArgumentException
    {
        // TODO
    }
}
