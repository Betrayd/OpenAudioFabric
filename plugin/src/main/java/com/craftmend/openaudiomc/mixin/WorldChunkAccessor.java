package com.craftmend.openaudiomc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Mixin (WorldChunk.class)
public interface WorldChunkAccessor {
    @Accessor
    World getWorld();
}
