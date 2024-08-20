package com.craftmend.openaudiomc.generic.migrations.migrations;

import com.craftmend.openaudiomc.generic.migrations.MigrationWorker;
import com.craftmend.openaudiomc.generic.migrations.interfaces.SimpleMigration;

public class PredictiveCacheMigration extends SimpleMigration {

    @Override
    public boolean shouldBeRun(MigrationWorker migrationWorker) {
        return false;
        // if (OpenAudioMc.getInstance().getPlatform() != Platform.SPIGOT) return false;

        // return new File(
        //         OpenAudioMcSpigot.getInstance().getDataFolder(), "cache.json"
        // ).exists();
    }

    @Override
    public void execute(MigrationWorker migrationWorker) {
        // OpenAudioLogger.info("Migrating world audio heatmap");
        // DatabaseService service = OpenAudioMc.getService(DatabaseService.class);
        // Repository<StoredWorldChunk> repo = service.getRepository(StoredWorldChunk.class);
        // try {
        //     SerializedAudioChunk.ChunkMap filemap = OpenAudioMc.getGson().fromJson(
        //             new String(Files.readAllBytes(new File(
        //                     OpenAudioMcSpigot.getInstance().getDataFolder(), "cache.json"
        //             ).toPath())),
        //             SerializedAudioChunk.ChunkMap.class
        //     );

        //     for (Map.Entry<String, SerializedAudioChunk.Chunk> entry : filemap.getData().entrySet()) {
        //         String name = entry.getKey();
        //         SerializedAudioChunk.Chunk chunk = entry.getValue();
        //         OpenAudioLogger.info("Migrating world section " + name);

        //         StoredWorldChunk swc = new StoredWorldChunk(name, chunk);
        //         repo.save(swc);
        //     }

        //     new File(
        //             OpenAudioMcSpigot.getInstance().getDataFolder(), "cache.json"
        //     ).delete();
        // } catch (IOException e) {
        //     OpenAudioLogger.error(e, "Failed to migrate world audio heatmap");
        // }
    }

}
