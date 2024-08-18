package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.media.time.TimeService;
import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.openaudiofabric.OpenAudioFabric;

import java.time.Duration;
import java.time.Instant;

public class TimeDetail implements StateDetail {

    @Override
    public String title() {
        return "TimeData";
    }

    @Override
    public String value() {
        return (OpenAudioFabric.getService(TimeService.class).getOffset() / 1000) + "compens, " + Duration.between(OpenAudioFabric.getService(TimeService.class).getLastUpdated(), Instant.now()).getSeconds() + " seconds ago";
    }

}
