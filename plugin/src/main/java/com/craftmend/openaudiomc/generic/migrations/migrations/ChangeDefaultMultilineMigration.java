package com.craftmend.openaudiomc.generic.migrations.migrations;

import com.craftmend.openaudiomc.generic.migrations.MigrationWorker;
import com.craftmend.openaudiomc.generic.migrations.interfaces.SimpleMigration;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.storage.interfaces.Configuration;
import com.openaudiofabric.OpenAudioFabric;

public class ChangeDefaultMultilineMigration extends SimpleMigration {

    @Override
    public boolean shouldBeRun(MigrationWorker migrationWorker) {
        Configuration config = OpenAudioFabric.getInstance().getConfiguration();
        return config.hasStorageKey(StorageKey.MESSAGE_CLICK_TO_CONNECT)
                && config.getString(StorageKey.MESSAGE_CLICK_TO_CONNECT).equals("&bClick &e&ohere&b to connect to the online audio client!");
    }

    @Override
    public void execute(MigrationWorker migrationWorker) {
        ignoredOldValues.add(StorageKey.MESSAGE_CLICK_TO_CONNECT);
        migrateFilesFromResources();
    }
}
