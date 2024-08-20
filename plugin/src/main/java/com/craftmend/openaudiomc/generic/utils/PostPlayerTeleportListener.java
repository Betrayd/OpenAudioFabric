package com.craftmend.openaudiomc.generic.utils;

import java.util.Set;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public interface PostPlayerTeleportListener {

    Event<PostPlayerTeleportListener> EVENT = EventFactory.createArrayBacked(PostPlayerTeleportListener.class,
            (listeners) -> (networkHandler, x, y, z, yaw, pitch, flags) -> {
                for (PostPlayerTeleportListener listener : listeners) {
                    listener.interact(networkHandler, x, y, z, yaw, pitch, flags);
                }
            });

    void interact(ServerPlayNetworkHandler handler, double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags);

}
