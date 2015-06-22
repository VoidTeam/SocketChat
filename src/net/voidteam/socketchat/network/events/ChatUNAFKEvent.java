/*
 * @date   6/22/15
 * @author Robert Duke
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this source code package.
 */
package net.voidteam.socketchat.network.events;

import net.voidteam.socketchat.network.events.iEvent;
import org.java_websocket.WebSocket;

public class ChatUNAFKEvent extends iEvent {
    public ChatUNAFKEvent(WebSocket client, String payload) {
        super(client, payload);
    }

    @Override
    public void run() throws IllegalArgumentException {

    }
}
