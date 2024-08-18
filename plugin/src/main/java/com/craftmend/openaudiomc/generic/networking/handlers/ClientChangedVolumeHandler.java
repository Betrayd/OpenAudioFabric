package com.craftmend.openaudiomc.generic.networking.handlers;

import com.craftmend.openaudiomc.generic.networking.abstracts.PayloadHandler;
import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.networking.interfaces.Authenticatable;
import com.craftmend.openaudiomc.generic.networking.payloads.in.ClientChangedVolumePayload;
import com.craftmend.openaudiomc.generic.node.packets.ClientUpdateStatePacket;
import com.craftmend.openaudiomc.generic.platform.Platform;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.openaudiofabric.OpenAudioFabric;

public class ClientChangedVolumeHandler extends PayloadHandler<ClientChangedVolumePayload> {

    @Override
    public void onReceive(ClientChangedVolumePayload payload) {
        Authenticatable authenticatable = findSession(payload.getClient());
        if (authenticatable instanceof ClientConnection) {
            ClientConnection connection = ((ClientConnection) authenticatable);
            connection.getSession().setVolume(payload.getVolume());

            // are we running in spigot? if not then we should forward this as a state change
            if (OpenAudioFabric.getInstance().getPlatform() != Platform.SPIGOT) {
                UserHooks hooks = OpenAudioFabric.getInstance().getInvoker().getUserHooks();
                hooks.sendPacket(connection.getUser(),
                        ClientUpdateStatePacket.of(connection)
                );
            }
        } else {
            // you don't even have volume
            authenticatable.kickConnection();
        }
    }
}
