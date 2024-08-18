package com.craftmend.openaudiomc.generic.networking.handlers;

import com.craftmend.openaudiomc.generic.networking.abstracts.PayloadHandler;
import com.craftmend.openaudiomc.generic.networking.interfaces.Authenticatable;
import com.craftmend.openaudiomc.generic.networking.interfaces.INetworkingEvents;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.networking.payloads.ClientConnectionPayload;
import com.openaudiofabric.OpenAudioFabric;

public class ClientConnectHandler extends PayloadHandler<ClientConnectionPayload> {

    @Override
    public void onReceive(ClientConnectionPayload payload) {
        Authenticatable authenticatable = findSession(payload.getUuid());
        if (authenticatable != null) {
            for (INetworkingEvents event : OpenAudioFabric.getService(NetworkingService.class).getEvents()) {
                event.onClientOpen(authenticatable);
            }
        }
    }
}
