package com.craftmend.openaudiomc.generic.migrations.migrations;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.migrations.interfaces.SimpleMigration;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.storage.interfaces.Configuration;

public class AddVcAnnouncementMigration extends SimpleMigration {

    @Override
    public boolean shouldBeRun() {
        Configuration config = OpenAudioMc.getInstance().getConfiguration();
        return !config.hasStorageKey(StorageKey.SETTINGS_VC_ANNOUNCEMENTS);
    }

    @Override
    public void execute() {
        migrateFilesFromResources();
    }
}
