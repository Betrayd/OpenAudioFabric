package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.openaudiofabric.OpenAudioFabric;

public class PlatformDetail implements StateDetail {
    @Override
    public String title() {
        return "Platform";
    }

    @Override
    public String value() {
        return OpenAudioFabric.getInstance().getPlatform().toString();
    }
}
