package com.craftmend.openaudiomc.spigot.modules.players.listeners;

import java.util.Set;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.utils.PostPlayerTeleportListener;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.modules.players.SpigotPlayerService;
import com.craftmend.openaudiomc.spigot.modules.players.objects.SpigotConnection;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PlayerTeleportationListener {

    private static PlayerTeleportationListener singleton = null;

    private PlayerTeleportationListener() {
        PostPlayerTeleportListener.EVENT.register((networkHandler, x,y,z,yaw,pitch,flags) -> 
        {
            onTp(networkHandler,x,y,z,yaw,pitch,flags);
        });
    }

    public static PlayerTeleportationListener create() {
        if (PlayerTeleportationListener.singleton == null) {
            PlayerTeleportationListener.singleton = new PlayerTeleportationListener();
        } else {
            OpenAudioLogger
                    .warn("tried to create a new PlayerTeleportationListener but one already exists! Passing old...");
        }
        return PlayerTeleportationListener.singleton;
    }

    //fabric listener runs after the event to see if we actually moved
    public void onTp(ServerPlayNetworkHandler handler, double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags) {
            // this event might be called before the player is registered, as some plugins
            // use
            // the teleport event to warp them to spawn, instead of the player spawn event
            if (!OpenAudioMc.getService(SpigotPlayerService.class).hasClient(handler.player))
                return;

            SpigotConnection spigotConnection = OpenAudioMc.getService(SpigotPlayerService.class)
                    .getClient(handler.player);
            if (spigotConnection == null)
                return;
            if (spigotConnection.getRegionHandler() != null) {
                spigotConnection.getRegionHandler().tick();
            }
            spigotConnection.getSpeakerHandler().tick();
    }

}
