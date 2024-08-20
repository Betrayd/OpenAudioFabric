package com.craftmend.openaudiomc.generic.utils;

import org.jetbrains.annotations.NotNull;

import com.mojang.logging.LogUtils;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.World;

/**
 * A re-implementation of some bukkit-specific methods.
 */
public class FabricUtils {

    public static MinecraftServer currentServer;

    public static String getWorldName(World world) {
        return world.getRegistryKey().getValue().toString();
    }

    public static ServerWorld getWorld(MinecraftServer server, @NotNull String world) {
        Identifier id;
        try {
            id = new Identifier(world);
        } catch (InvalidIdentifierException e) {
            LogUtils.getLogger().error("Unable to get world from string.", e);
            return null;
        }

        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
        return server.getWorld(key);
    }

}
