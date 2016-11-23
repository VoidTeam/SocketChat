package net.voidteam.socketchat.network.events;

import org.java_websocket.WebSocket;

public class ChatBlurEvent extends iEvent
{
    public ChatBlurEvent(WebSocket client, String payload)
    {
        super(client, payload);
    }

    /**
     * Chat box has lost focus.
     *
     * @throws IllegalArgumentException
     */
    @Override
    public void run() throws IllegalArgumentException
    {
        // TODO
    }
}
