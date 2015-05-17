package net.voidteam.socketchat;

import java.io.File;
import java.util.UUID;

import net.minecraft.server.v1_8_R2.*;
import org.bukkit.craftbukkit.v1_8_R2.*;
import com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

public class OfflinePlayerLoader {
	public static Player load(String exactPlayerName) {
		// big thanks to
		// https://github.com/lishd/OpenInv/blob/master/src/com/lishid/openinv/internal/craftbukkit/PlayerDataManager.java
		// Offline inv here...
		
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(exactPlayerName);
		
		int index = 0;
		for (World w : Bukkit.getWorlds()) {
			try {
				// See if the player has data files

				// Find the player folder
				File playerfolder = new File(w.getWorldFolder(), "players");
				if (!playerfolder.exists()) {
					playerfolder = new File(w.getWorldFolder(), "playerdata");
				}
				
				if(!playerfolder.exists()) {
					continue;
				}
				
				Player target = null;
				MinecraftServer server = null;
				try {
					server = ((CraftServer) Bukkit.getServer()).getServer();
				} catch (Exception e) {
					server = ((CraftServer) Bukkit.getServer()).getHandle().getServer();
				}

				// Create an entity to load the player data
				UUID id = null;
				
				if(player == null) {
					id = UUID.randomUUID();
				}
				else {
					id = player.getUniqueId();
				}
				
				EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(index), new GameProfile(id, exactPlayerName), new PlayerInteractManager(server.getWorldServer(index)));
				

				// Get the bukkit entity
				target = (entity == null) ? null : entity.getBukkitEntity();

				if (target != null) {
					// Load data
					target.loadData();
					// Return the entity
					return target;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			} finally {
				index++;
			}
		}

		return null;
	}
}
