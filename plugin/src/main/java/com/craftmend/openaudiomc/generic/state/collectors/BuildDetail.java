package com.craftmend.openaudiomc.generic.state.collectors;

import com.craftmend.openaudiomc.generic.environment.GlobalConstantService;
import com.craftmend.openaudiomc.generic.environment.models.ProjectStatus;
import com.craftmend.openaudiomc.generic.state.interfaces.StateDetail;
import com.openaudiofabric.OpenAudioFabric;

public class BuildDetail implements StateDetail {

    @Override
    public String title() {
        return "Build";
    }

    @Override
    public String value() {
        ProjectStatus ps = OpenAudioFabric.getService(GlobalConstantService.class).getProjectStatus();
        String l = "";
        if (ps == null) {
            l = "Unknown";
        } else {
            l = ps.getLatestBuildNumber() + "";
        }

        return "Running build: " + OpenAudioFabric.BUILD.getBuildNumber() + ", latest: " + l;
    }

}
