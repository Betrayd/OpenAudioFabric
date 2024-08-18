package com.craftmend.openaudiomc.spigot.modules.voicechat.tasks;

import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.networking.packets.client.voice.PacketClientUpdateVoiceLocations;
import com.craftmend.openaudiomc.generic.networking.payloads.client.voice.ClientVoiceUpdatePeerLocationsPayload;
import com.openaudiofabric.OpenAudioFabric;

import java.util.HashSet;

public class TickVoicePacketQueue implements Runnable {

    @Override
    public void run() {
        for (ClientConnection client : OpenAudioFabric.getService(NetworkingService.class).getClients()) {
            if (!client.getRtcSessionManager().getLocationUpdateQueue().isEmpty()) {
                client.sendPacket(new PacketClientUpdateVoiceLocations(
                        new ClientVoiceUpdatePeerLocationsPayload(
                                new HashSet<>(client.getRtcSessionManager().getLocationUpdateQueue())
                        )
                ));
                client.getRtcSessionManager().getLocationUpdateQueue().clear();
            }
        }
    }
}
