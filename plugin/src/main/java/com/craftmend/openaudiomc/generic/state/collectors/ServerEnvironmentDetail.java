package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.openaudiofabric.OpenAudioFabric;

public class ServerEnvironmentDetail implements StateDetail {
    @Override
    public String title() {
        return "Env";
    }

    @Override
    public String value() {
        return OpenAudioFabric.SERVER_ENVIRONMENT.name();
    }
}
