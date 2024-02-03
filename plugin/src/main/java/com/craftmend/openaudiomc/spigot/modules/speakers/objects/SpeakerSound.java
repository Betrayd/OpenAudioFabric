package com.craftmend.openaudiomc.spigot.modules.speakers.objects;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.media.enums.MediaFlag;
import com.craftmend.openaudiomc.generic.media.objects.Sound;
import lombok.Getter;
import lombok.Setter;

public class SpeakerSound extends Sound {

    @Getter @Setter
    private boolean distanceFading = false;

    public SpeakerSound(String source) {
        super(source);
        setLoop(true);
        setDoPickup(OpenAudioMc.getInstance().getConfiguration().getBoolean(StorageKey.SETTINGS_SPEAKER_SYNC));
        setFadeTime(100);
        setFlag(MediaFlag.SPEAKER);
    }

}
