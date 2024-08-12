package com.craftmend.openaudiomc.spigot.modules.speakers.objects;

import com.craftmend.openaudiomc.api.speakers.Loc;
import com.craftmend.openaudiomc.generic.utils.Location;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Data
@AllArgsConstructor
public class MappedLocation implements Loc {

    private int x;
    private int y;
    private int z;
    private String world;

    public MappedLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getRegistryKey().getValue().toString();
    }

    public Location toLocation(MinecraftServer server) {
        return new Location(server.getRegistryManager().get(RegistryKeys.WORLD).get(Identifier.tryParse(this.world)), this.x, this.y, this.z);
    }

    public BlockState getBlockState(MinecraftServer server) {
        World world = parseWorld(server);
        if (world != null) return world.getBlockState(new BlockPos(this.x, this.y, this.z));
        return null;
    }

    public BlockEntity getBlockEntity(MinecraftServer server) {
        World world = parseWorld(server);
        if (world != null)
        {
            return world.getBlockEntity(new BlockPos(this.x, this.y, this.z));
        }
        return null;
    }

    public static MappedLocation fromLocation(Location location) {
        return new MappedLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getRegistryKey().toString());
    }

    private World parseWorld(MinecraftServer server)
    {
        return server.getRegistryManager().get(RegistryKeys.WORLD).get(Identifier.tryParse(this.world));
    }
}
