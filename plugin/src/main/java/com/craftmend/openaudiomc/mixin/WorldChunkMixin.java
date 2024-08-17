package com.craftmend.openaudiomc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.craftmend.openaudiomc.generic.utils.BlockStateChangedCallback;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Mixin  (WorldChunk.class)
public class WorldChunkMixin {

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z;)Lnet/minecraft/block/BlockState", at = @At("HEAD"), cancellable = true)
        private void openaudio$setBlockState(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        World world = ((WorldChunkAccessor)((WorldChunk)((Object)(this)))).getWorld();
        BlockStateChangedCallback.EVENT.invoker().interact(pos, state, moved, world, cir);
    }
}
