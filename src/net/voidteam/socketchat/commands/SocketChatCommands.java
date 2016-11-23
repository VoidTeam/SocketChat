package net.voidteam.socketchat.commands;

import net.voidteam.socketchat.JoinLeavePackets;
import net.voidteam.socketchat.SocketChat;
import net.voidteam.socketchat.network.SocketListener;
import net.voidteam.socketchat.network.events.SSOAuthorizeEvent;
import net.voidteam.socketchat.Utilities;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SocketChatCommands implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        // Kick all players from WebChat.
        if (label.equals("wkickall")) {
            if (!sender.hasPermission("socketchat.kickall")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                return true;
            }

            SocketChat.kickall(StringUtils.join(args, " "));

            return true;
        }

        // Kick the specified player from WebChat.
        if (label.equals("wkick")) {
            if (!sender.hasPermission("socketchat.kick")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");

                return true;
            }

            if (args.length < 1) {
                return false;
            }

            if (args.length == 1) {
                if (!SocketChat.kick(args[0], "")) {
                    sender.sendMessage(ChatColor.RED + args[0] + " is not in webchat!");
                }
            }

            if (args.length > 1) {
                if (!SocketChat.kick(args[0], StringUtils.join(args, " ", 1, args.length))) {
                    sender.sendMessage(ChatColor.RED + args[0] + " is not in webchat!");
                }
            }

            return true;
        }

        // Info message.
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.AQUA + "[SocketChat] Made by Robby Duke (a.k.a NoEff3x).");

            return true;
        }

        if (args.length == 1)
        {
            // List online WebChat players.
            if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("who"))
            {
                List<String> onlineList = new ArrayList<>();

                for (String username : SocketListener.activeSessions.values()) {
                    if (SSOAuthorizeEvent.spyList.contains(username)) {
                        if (sender.hasPermission("socketchat.spy")) {
                            onlineList.add("&8[HIDDEN]&e" + username + "&f");
                        }
                    } else {
                        onlineList.add("&e" + username + "&f");
                    }
                }

                if (!Utilities.implode(onlineList).equals("")) {
                    sender.sendMessage(ChatColor.GRAY + "Webchat: " + ChatColor.translateAlternateColorCodes('&', Utilities.implode(onlineList).substring(2)));

                    return true;
                } else {
                    return true;
                }
            }

            // Kick all WebChat players.
            if (args[0].equalsIgnoreCase("kickall"))
            {
                if (!sender.hasPermission("socketchat.kickall")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");

                    return true;
                }

                SocketChat.kickall("");

                return true;
            }

            // Fake join via WebChat.
            if (args[0].equalsIgnoreCase("fj"))
            {
                if (!sender.hasPermission("vanish.fakeannounce")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");

                    return true;
                }
                JoinLeavePackets.joinServer(sender.getName());
                return true;
            }

            // Fake quit via WebChat.
            if (args[0].equalsIgnoreCase("fq"))
            {
                if (!sender.hasPermission("vanish.fakeannounce")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");

                    return true;
                }

                JoinLeavePackets.leaveServer(sender.getName());

                return true;
            }
            return false;
        }

        if (args.length > 1)
        {
            // Kick specified player from WebChat.
            if (args[0].equalsIgnoreCase("kick"))
            {
                if (!sender.hasPermission("socketchat.kick")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");

                    return true;
                }

                if (!SocketChat.kick(args[1], StringUtils.join(args, " ", 2, args.length))) {
                    sender.sendMessage(ChatColor.RED + args[0] + " is not in webchat!");
                }
            }

            // Kick all WebChat players.
            if (args[0].equalsIgnoreCase("kickall"))
            {
                if (!sender.hasPermission("socketchat.kickall")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");

                    return true;
                }

                SocketChat.kickall(StringUtils.join(args, " ", 1, args.length));
            }

            return true;
        }

        return false;
    }
}
