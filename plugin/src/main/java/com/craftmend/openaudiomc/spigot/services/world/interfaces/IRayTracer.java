package com.craftmend.openaudiomc.spigot.services.world.interfaces;

import net.minecraft.util.math.Vec3d;

public interface IRayTracer {

    // count the obstructions (walls etc) between two locations
    int obstructionsBetweenLocations(Vec3d start, Vec3d end);

}
