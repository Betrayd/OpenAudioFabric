package com.craftmend.openaudiomc.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.craftmend.openaudiomc.generic.utils.PostPlayerTeleportListener;

import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(method = "requestTeleport(DDDFFLjava/util/Set<Lnet/minecraft/network/packet/s2c/play/PositionFlag;>;)V", at = @At("TAIL"), cancellable = true)
    private void openaudio$requestTeleport(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags) {
        
        PostPlayerTeleportListener.EVENT.invoker().interact(((ServerPlayNetworkHandler)((Object)this)), x, y, z, yaw, pitch, flags);
    }
}
