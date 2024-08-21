package com.openaudiofabric.modules.player;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.authentication.AuthenticationService;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.user.adapters.CommandSenderUserAdapter;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PlayerConnectionListener {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register(PlayerConnectionListener::onPostLogin);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionListener::onLogout);
    }

    public static void onPostLogin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        if(OpenAudioMc.getInstance() == null || OpenAudioMc.getInstance().isDisabled() == true)
        {
            OpenAudioLogger.info("post login failed ; for some reason openAUdio was down");
            return;
        }
        if(OpenAudioMc.getService(AuthenticationService.class).isSuccessful())
        {
            OpenAudioMc.getService(NetworkingService.class).register(new CommandSenderUserAdapter(handler.getPlayer()), null);   
        }
    }

    public static void onLogout(ServerPlayNetworkHandler handler, MinecraftServer server) {
        if(OpenAudioMc.getInstance() == null || OpenAudioMc.getInstance().isDisabled() == true)
        {
            OpenAudioLogger.info("logout failed ; for some reason openAUdio was down");
            return;
        }
        if(OpenAudioMc.getService(AuthenticationService.class).isSuccessful())
        {
            OpenAudioMc.getService(NetworkingService.class).remove(handler.getPlayer().getUuid());
        }
    }
}
