package com.craftmend.openaudiomc.spigot.modules.speakers.tasks;

import com.craftmend.openaudiomc.generic.database.DatabaseService;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.platform.interfaces.TaskService;
import com.craftmend.openaudiomc.generic.storage.enums.GcStrategy;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.utils.Location;
import com.craftmend.openaudiomc.generic.utils.OARunnable;
import com.craftmend.openaudiomc.spigot.modules.speakers.SpeakerService;
import com.craftmend.openaudiomc.spigot.modules.speakers.objects.MappedLocation;
import com.craftmend.openaudiomc.spigot.modules.speakers.objects.Speaker;
import com.craftmend.openaudiomc.spigot.modules.speakers.utils.SpeakerUtils;
import com.openaudiofabric.OpenAudioFabric;

import net.minecraft.server.MinecraftServer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpeakerGarbageCollection extends OARunnable {

    private SpeakerService speakerService;
    private static int PROCESSED_SPEAKERS = 0;
    private int lastFraction = 0;
    private final int FRACTION_GROUP_SIZE = 50;
    private boolean forceRun = false;

    private final MinecraftServer server;

    public SpeakerGarbageCollection(MinecraftServer server, SpeakerService speakerService) {
        super();
        this.server = server;
        this.speakerService = speakerService;
        runTaskTimer(600, 600);
        OpenAudioFabric.resolveDependency(TaskService.class).scheduleAsyncRepeatingTask(() -> {
            if (PROCESSED_SPEAKERS != 0) {
                OpenAudioLogger.info("The garbage collector found and processed " + PROCESSED_SPEAKERS + " broken speakers");
                PROCESSED_SPEAKERS = 0;
            }
        }, 20 * 30, 20 * 30);
    }

    public SpeakerGarbageCollection(MinecraftServer server) {
        super();
        this.server = server;
        this.forceRun = true;
        this.speakerService = OpenAudioFabric.getService(SpeakerService.class);
    }

    @Override
    public void run() {
        int maxFractions = forceRun ? 999999999 : roundUp(this.speakerService.getSpeakerMap().values().size(), FRACTION_GROUP_SIZE);

        // fraction logic to break computing into smaller parts
        int fractionStart = lastFraction * FRACTION_GROUP_SIZE;

        lastFraction++;
        if (maxFractions > lastFraction) {
            lastFraction = 0;
        }

        int setSize = this.speakerService.getSpeakerMap().values().size();
        possiblyFilterLimits(setSize, this.speakerService
                .getSpeakerMap()
                .values().stream()
                        .filter(speaker -> !speaker.getValidated())
                        .skip(fractionStart)
        ).collect(Collectors.toList())
                .forEach(speaker -> {
                    MappedLocation mappedLocation = speaker.getLocation();
                    if (mappedLocation == null) {
                        OpenAudioLogger.warn("A speaker doesn't have a location, terminating");
                        remove(speaker);
                        return;
                    }

                    // check if the chunk is loaded, if not, don't do shit lmao
                    Location bukkitLocation = mappedLocation.toLocation(server);
                    if (bukkitLocation == null || bukkitLocation.getWorld() == null) {
                        OpenAudioLogger.warn("Can't find world " + mappedLocation.getWorld() + " so speaker " + speaker.getSpeakerId() + " is being deleted");
                        remove(speaker);
                    } 
                    else {
                        boolean isChunkLoaded = bukkitLocation.getWorld().getChunkManager().isChunkLoaded(bukkitLocation.getChunkPos().x, bukkitLocation.getChunkPos().z);
                        if (isChunkLoaded || forceRun) {
                            if (forceRun && !isChunkLoaded) {
                                OpenAudioLogger.info("Attempting to load chunk " + bukkitLocation.getChunk().toString() + " for a forced speaker check...");
                                if (!isChunkLoaded) {
                                    OpenAudioLogger.warn("Failed to load chunk! please try again later...");
                                }
                                return;
                            }
    
                            if (!SpeakerUtils.isSpeakerSkull(speaker.getLocation().getBlockEntity(server))) {
                                remove(speaker);
                            } else {
                                speaker.setValidated(true);
                            }
                        }
                    }
                });
    }

    private void remove(Speaker speaker) {
        GcStrategy strategy = GcStrategy.valueOf(StorageKey.SETTINGS_GC_STRATEGY.getString());
        if (strategy == GcStrategy.DELETE) {
            OpenAudioFabric.getService(DatabaseService.class)
                    .getRepository(Speaker.class)
                    .delete(speaker);
        }
        OpenAudioFabric.resolveDependency(TaskService.class).runAsync(() -> {
            speakerService.getSpeakerMap().remove(speaker.getLocation());
        });
        PROCESSED_SPEAKERS++;
    }

    private Stream<Speaker> possiblyFilterLimits(int size, Stream<Speaker> stream) {
        if (size > 250) {
            return stream.limit(FRACTION_GROUP_SIZE);
        }
        return stream;
    }

    public int roundUp(long num, long divisor) {
        int sign = (num > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);
        return (int) (sign * (Math.abs(num) + Math.abs(divisor) - 1) / Math.abs(divisor));
    }
}
