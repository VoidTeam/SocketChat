package net.voidteam.socketchat.network.events;

import org.java_websocket.WebSocket;

public abstract class iEvent
{
    private final String payload;
    private final WebSocket client;

    iEvent(WebSocket client, String payload)
    {
        this.payload = payload;
        this.client = client;
    }

    /**
     * Client/Socket that this event belongs to.
     *
     * @return WebSocket
     */
    public final WebSocket getClient()
    {
        return this.client;
    }

    /**
     * Contents of this event.
     *
     * @return String
     */
    public final String getPayload()
    {
        return this.payload;
    }

    /**
     * Process to occur when this event happens.
     *
     * @throws IllegalArgumentException
     */
    public abstract void run() throws IllegalArgumentException;
}
