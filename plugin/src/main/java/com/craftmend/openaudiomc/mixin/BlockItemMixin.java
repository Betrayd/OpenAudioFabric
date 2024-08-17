package com.craftmend.openaudiomc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.craftmend.openaudiomc.generic.utils.BlockPlaceCallback;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;

@Mixin (BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void openaudio$place(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ActionResult result = BlockPlaceCallback.EVENT.invoker().interact(context, state);

        if(result == ActionResult.FAIL) {
            cir.setReturnValue(false);
        }
        else if(result == ActionResult.CONSUME) {
            cir.setReturnValue(true);
        }
    }
}
