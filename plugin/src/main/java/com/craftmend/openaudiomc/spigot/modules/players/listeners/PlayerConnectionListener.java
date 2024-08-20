package com.craftmend.openaudiomc.spigot.modules.players.listeners;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.spigot.modules.players.SpigotPlayerService;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PlayerConnectionListener {

    private static PlayerConnectionListener singleton = null;

    private PlayerConnectionListener() {
        ServerPlayConnectionEvents.JOIN.register((networkHandler, packetSender, server)->
        {
            this.onJoin(networkHandler);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((networkHandler, server)->
        {
            this.onQuit(networkHandler);
        });
    }

    public static PlayerConnectionListener create() {
        if (PlayerConnectionListener.singleton == null) {
            PlayerConnectionListener.singleton = new PlayerConnectionListener();
        } else {
            OpenAudioLogger.warn("tried to create a new PlayerConnectionListener but one already exists! Passing old...");
        }
        return PlayerConnectionListener.singleton;
    }

    public void onJoin(ServerPlayNetworkHandler handler) {
        OpenAudioMc.getService(SpigotPlayerService.class).register(handler.getPlayer());
    }

    public void onQuit(ServerPlayNetworkHandler handler) {
        OpenAudioMc.getService(SpigotPlayerService.class).remove(handler.getPlayer());
    }

}
