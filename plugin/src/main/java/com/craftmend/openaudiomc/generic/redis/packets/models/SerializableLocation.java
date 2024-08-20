package com.craftmend.openaudiomc.generic.redis.packets.models;

import com.craftmend.openaudiomc.generic.utils.FabricUtils;
import com.craftmend.openaudiomc.generic.utils.Location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.minecraft.world.World;

@Builder(toBuilder = true)
@AllArgsConstructor
public class SerializableLocation {

    @Getter private double x;
    @Getter private double y;
    @Getter private double z;
    @Getter private float pitch;
    @Getter private float yaw;
    @Getter private String world;
    private transient Location cachedBukkit; // TRANSIENT! NEVER SERIALIZE OR I WILL FUCK YOU UP! ITS JUST A CACHE!

    public static SerializableLocation fromBukkit(Location location) {
        return SerializableLocation
                .builder()
                .x(location.getX())
                .y(location.getY())
                .z(location.getZ())
                .pitch(location.getPitch())
                .yaw(location.getYaw())
                .world(FabricUtils.getWorldName(location.getWorld()))
                .build();
    }

    public Location toBukkit() {
        if (cachedBukkit != null) return cachedBukkit;
        World fabricWorld = FabricUtils.getWorld(FabricUtils.currentServer, world);
        cachedBukkit = new Location(
                fabricWorld,
                x,
                y,
                z,
                yaw,
                pitch);
        return toBukkit();
    }

}
