package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.state.StateService;
import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.openaudiofabric.OpenAudioFabric;

public class GeneralStateDetail implements StateDetail {
    @Override
    public String title() {
        return "State";
    }

    @Override
    public String value() {
        return OpenAudioFabric.getService(StateService.class).getCurrentState().getClass().getSimpleName() + " - " + OpenAudioFabric.getService(StateService.class).getCurrentState().getDescription();
    }
}
