package com.craftmend.openaudiomc.generic.utils;

import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Vector3 implements Serializable {

    public static final Vector3 EMPTY = new Vector3();

    private double x = 0;
    private double y = 0;
    private double z = 0;

    public static Vector3 from(ClientConnection peer) {
        PlayerEntity player = (PlayerEntity) peer.getUser().getOriginal();
        return from(Location.fromEntity(player));
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector3)) return false;
        Vector3 otherVector = (Vector3) other;

        return x == otherVector.getX() && y == otherVector.getY() && z == otherVector.getZ();
    }

    /*public Vector toBukkit() {
        return new Vector(x, y, z);
    }*/

    public static Vector3 from(Location location) {
        return new Vector3(
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    public static Vector3 from(MappedLocation location) {
        return new Vector3(
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }
}
