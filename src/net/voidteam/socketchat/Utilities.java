package net.voidteam.socketchat;

import java.util.logging.Level;

/**
 * Created by Robby Duke on 6/19/14.
 * Copyright (c) 2014
 *
 * @project SocketChat
 * @time 12:54 AM
 */
public class Utilities {
    private static final boolean debugMode = true;
    private static final String prefix = "[SocketChat] ";

    /**
     * Display any debuggable information to the console.
     * @param debugMessage Message to be displayed to the console.
     */
    public static void debug(String debugMessage) {
        if (debugMode) SocketChat.getPlugin().getLogger().log(Level.INFO, prefix.concat(debugMessage));
    }

    /**
     * Display any runtime information to the console.
     * @param logMessage Message to be displayed to the console.
     */
    public static void log(String logMessage) {
        SocketChat.getPlugin().getLogger().log(Level.INFO, prefix.concat(logMessage));
    }

    /**
     * Display any runtime errors to the console.
     * @param severeMessage Message to be displayed to the console.
     */
    public static void severe(String severeMessage) {
        SocketChat.getPlugin().getLogger().log(Level.SEVERE, prefix.concat(severeMessage));
    }
}
