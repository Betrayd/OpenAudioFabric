package com.craftmend.openaudiomc.generic.proxy.messages.implementations;

import java.util.List;

import com.craftmend.openaudiomc.generic.proxy.messages.PacketManager;
import com.craftmend.openaudiomc.generic.proxy.messages.PacketPlayer;
import com.craftmend.openaudiomc.generic.utils.FabricUtils;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class FabricPacketManager extends PacketManager {

    public FabricPacketManager(String channel) {
        super(channel);
    }

    private final Random random = Random.create();

    @Override
    protected void sendPluginMessage(PacketPlayer packetPlayer, String channel, byte[] bytes) {
        // MinecraftServer server = packetPlayer.getFabricPlayer().getServer();'
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(bytes);
        ServerPlayNetworking.send(packetPlayer.getFabricPlayer(), new Identifier(channel), buf);
    }

    @Override
    protected int getPlayerCount() {
        return FabricUtils.currentServer.getCurrentPlayerCount();
    }

    @Override
    protected PacketPlayer getRandomPlayer() {
        List<ServerPlayerEntity> players = FabricUtils.currentServer.getPlayerManager().getPlayerList();
        if (players.isEmpty())
            return null;
        
        int randPlayer = random.nextBetweenExclusive(0, players.size());
        return new PacketPlayer(players.get(randPlayer));
    }
    
}
