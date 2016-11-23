package net.voidteam.socketchat.network.events;

import org.java_websocket.WebSocket;

public class ChatFocusEvent extends iEvent
{
    public ChatFocusEvent(WebSocket client, String payload)
    {
        super(client, payload);
    }

    /**
     * Chat box has regained focus.
     *
     * @throws IllegalArgumentException
     */
    @Override
    public void run() throws IllegalArgumentException
    {
        // TODO
    }
}
