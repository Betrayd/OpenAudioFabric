package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.modules.speakers.SpeakerService;
import com.openaudiofabric.OpenAudioFabric;

public class SpigotSpeakerDetail implements StateDetail {
    @Override
    public String title() {
        return "Loaded Speakers";
    }

    @Override
    public String value() {
        return OpenAudioFabric.getService(SpeakerService.class).getSpeakerMap().size() + "";
    }
}
