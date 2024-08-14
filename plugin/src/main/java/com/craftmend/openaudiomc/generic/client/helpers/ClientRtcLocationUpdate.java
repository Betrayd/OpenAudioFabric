package com.craftmend.openaudiomc.generic.client.helpers;

import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.utils.Location;
import com.craftmend.openaudiomc.spigot.services.world.Vector3;
import com.craftmend.openaudiomc.spigot.services.world.interfaces.IRayTracer;
import com.craftmend.openaudiomc.spigot.services.world.tracing.DummyTracer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;

@NoArgsConstructor
@AllArgsConstructor
public class ClientRtcLocationUpdate {

    private static final boolean PROCESS_OBSTRUCTIONS = StorageKey.SETTINGS_VC_PROCESS_OBSTRUCTIONS.getBoolean();
    private static IRayTracer rayTracer = new DummyTracer();

    private String streamKey;
    private double x, y, z;
    private int obstructions;

    public static ClientRtcLocationUpdate fromClientWithLocation(ClientConnection clientConnection, Location source, Vector3 targetLocation) {
        int obstructions = 0;

        if (PROCESS_OBSTRUCTIONS) {
            // check line-of-sight
            obstructions = rayTracer.obstructionsBetweenLocations(
                    source,
                    targetLocation
            );

        }

        return new ClientRtcLocationUpdate(
                clientConnection.getRtcSessionManager().getStreamKey(),
                source.getX(),
                source.getY(),
                source.getZ(),
                obstructions
        );
    }

    public static ClientRtcLocationUpdate fromClient(ClientConnection clientConnection, Vector3 originLocation) {
        PlayerEntity player = (PlayerEntity) clientConnection.getUser().getOriginal();

        int obstructions = 0;

        if (PROCESS_OBSTRUCTIONS) {
            // check line-of-sight
            obstructions = rayTracer.obstructionsBetweenLocations(
                    Location.locationFromEntity(player),
                    originLocation
            );

        }

        return new ClientRtcLocationUpdate(
                clientConnection.getRtcSessionManager().getStreamKey(),
                player.getX(),
                player.getY(),
                player.getZ(),
                obstructions
        );
    }

}
