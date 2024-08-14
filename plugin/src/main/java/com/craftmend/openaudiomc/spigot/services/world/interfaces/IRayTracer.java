package com.craftmend.openaudiomc.spigot.services.world.interfaces;

import com.craftmend.openaudiomc.generic.utils.Location;
import com.craftmend.openaudiomc.spigot.services.world.Vector3;

public interface IRayTracer {

    // count the obstructions (walls etc) between two locations
    int obstructionsBetweenLocations(Location start, Vector3 end);

}
