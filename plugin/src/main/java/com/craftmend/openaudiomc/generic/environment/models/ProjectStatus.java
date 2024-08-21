package com.craftmend.openaudiomc.generic.environment.models;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.rest.response.AbstractRestResponse;
import lombok.Getter;

@Getter
public class ProjectStatus extends AbstractRestResponse {

    private VersionDetails versioning;
    private Announcement announcement;

    public boolean isLocalLatest() {
        //TODO: actually fix the OpenAudioMC.BUILD system
        //the chance of updating this is low so... yeah probably
        return true;
        //return OpenAudioMc.BUILD.getBuildNumber() >= versioning.getBuildNumber();
    }

    public int getLatestBuildNumber() {
        return versioning.getBuildNumber();
    }

    public boolean isAnnouncementAvailable() {
        return announcement.getHasAnnouncement();
    }

}
