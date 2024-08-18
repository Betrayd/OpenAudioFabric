package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.services.server.ServerService;
import com.openaudiofabric.OpenAudioFabric;

public class SpigotVersionDetail implements StateDetail {

    @Override
    public String title() {
        return "SrvVersion";
    }

    @Override
    public String value() {
        return OpenAudioFabric.getService(ServerService.class).getVersion().toString();
    }

}
