package com.craftmend.openaudiomc.generic.utils;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;

public interface BlockPlaceCallback {
 
    Event<BlockPlaceCallback> EVENT = EventFactory.createArrayBacked(BlockPlaceCallback.class,
        (listeners) -> (context, blockstate) -> {
            for (BlockPlaceCallback listener : listeners) {
                ActionResult result = listener.interact(context, blockstate);
 
                if(result != ActionResult.PASS) {
                    return result;
                }
            }
 
        return ActionResult.PASS;
    });
 
    ActionResult interact(ItemPlacementContext context, BlockState state);
}
