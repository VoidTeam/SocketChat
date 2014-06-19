package net.voidteam.socketchat.network.events;

import org.java_websocket.WebSocket;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 10:54 AM
 */
public abstract class iEvent {
    private final String payload;
    private final WebSocket client;

    public iEvent(WebSocket client, String payload) {
        this.payload = payload;
        this.client = client;
    }

    public final WebSocket getClient() {
        return this.client;
    }

    public final String getPayload() {
        return this.payload;
    }

    public abstract void run() throws IllegalArgumentException;
}
