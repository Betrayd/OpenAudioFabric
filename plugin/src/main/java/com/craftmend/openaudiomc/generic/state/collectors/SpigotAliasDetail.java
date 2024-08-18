package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.modules.shortner.AliasService;
import com.openaudiofabric.OpenAudioFabric;

public class SpigotAliasDetail implements StateDetail {

    @Override
    public String title() {
        return "Aliases";
    }

    @Override
    public String value() {
        return OpenAudioFabric.getService(AliasService.class).getAliasMap().size() + "";
    }

}
