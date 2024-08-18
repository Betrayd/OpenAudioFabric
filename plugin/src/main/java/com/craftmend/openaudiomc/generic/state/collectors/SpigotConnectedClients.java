package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.craftmend.openaudiomc.spigot.modules.players.SpigotPlayerService;
import com.craftmend.openaudiomc.spigot.modules.players.objects.SpigotConnection;
import com.openaudiofabric.OpenAudioFabric;

public class SpigotConnectedClients implements StateDetail {

    @Override
    public String title() {
        return "Connected Clients";
    }

    @Override
    public String value() {
        int clients = 0;
        for (SpigotConnection spigotConnection : OpenAudioFabric.getService(SpigotPlayerService.class).getClients()) {
            if (spigotConnection.getClientConnection().isConnected()) clients++;
        }
        return clients + "";
    }

}
