package com.craftmend.openaudiomc.generic.utils;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockStateChangedCallback {
        Event<BlockStateChangedCallback> EVENT = EventFactory.createArrayBacked(BlockStateChangedCallback.class,
        (listeners) -> (pos, state, moved, world, cir) -> {
            for (BlockStateChangedCallback listener : listeners) {
                listener.interact(pos, state, moved, world, cir);
            }
    });
 
    void interact(BlockPos pos, BlockState state, boolean moved, World world, CallbackInfoReturnable<BlockState> cir);
}
