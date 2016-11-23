package net.voidteam.socketchat;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.MinecraftServer;
import net.minecraft.server.v1_10_R1.PlayerInteractManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

public class OfflinePlayerLoader
{
    /**
     * Load offline player.
     * Thanks to OpenInv for this:
     * https://github.com/lishid/OpenInv/blob/master/src/main/java/com/lishid/openinv/internal/PlayerDataManager.java
     *
     * @param username Player's username.
     * @return Player
     */
    @SuppressWarnings("deprecation")
    public static Player load(String username)
    {
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(username);

            if (player == null || !player.hasPlayedBefore()) {
                return null;
            }

            GameProfile profile = new GameProfile(player.getUniqueId(), player.getName());
            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

            // Create an entity to load the player data.
            EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));

            // Get the bukkit entity.
            Player target = entity.getBukkitEntity();

            if (target != null) {
                // Load data.
                target.loadData();

                // Return the entity.
                return target;
            }
        } catch (Exception e) {
            Utilities.log(e.getMessage());
        }

        return null;
    }
}
