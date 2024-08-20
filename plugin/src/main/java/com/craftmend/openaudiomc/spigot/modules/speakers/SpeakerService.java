package com.craftmend.openaudiomc.spigot.modules.speakers;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.database.DatabaseService;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.media.MediaService;
import com.craftmend.openaudiomc.generic.service.Inject;
import com.craftmend.openaudiomc.generic.service.Service;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.generic.utils.CustomPayloadOARunnable;
import com.craftmend.openaudiomc.generic.utils.Location;
import com.craftmend.openaudiomc.spigot.modules.players.SpigotPlayerService;
import com.craftmend.openaudiomc.spigot.modules.players.objects.SpigotConnection;
import com.craftmend.openaudiomc.api.speakers.ExtraSpeakerOptions;
import com.craftmend.openaudiomc.api.speakers.SpeakerType;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.services.world.interfaces.IRayTracer;
//import com.craftmend.openaudiomc.spigot.modules.speakers.listeners.SpeakerSelectListener;
import com.craftmend.openaudiomc.spigot.modules.speakers.objects.*;
import com.craftmend.openaudiomc.spigot.modules.speakers.tasks.SpeakerGarbageCollection;
import com.craftmend.openaudiomc.spigot.services.world.tracing.DummyTracer;
import com.openaudiofabric.OpenAudioFabric;
import com.craftmend.openaudiomc.spigot.services.server.ServerService;
import com.craftmend.openaudiomc.spigot.services.server.enums.ServerVersion;
import com.craftmend.openaudiomc.spigot.modules.speakers.listeners.SpeakerCreateListener;
import com.craftmend.openaudiomc.spigot.modules.speakers.listeners.SpeakerDestroyListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class SpeakerService extends Service {

    @Inject
    private OpenAudioMcSpigot openAudioMcSpigot;

    @Inject
    private DatabaseService databaseService;

    @Getter private SpeakerCollector collector;

    public static final SpeakerType DEFAULT_SPEAKER_TYPE = SpeakerType.SPEAKER_3D;
    @Getter private final Map<MappedLocation, Speaker> speakerMap = new ConcurrentHashMap<>();
    private final Map<String, SpeakerMedia> speakerMediaMap = new ConcurrentHashMap<>();
    @Getter private Item playerSkullItem;

    private SpeakerDestroyListener destroyListener;

    //Removed this because I was too lazy to add the setblock command and read the MC code to see how to do that with a skull.
    //I also accidentally hard coded some of these to be player heads when they should not have been I think but whatever
    //@Getter private BlockState playerSkullBlock;

    //@Getter private ServerVersion version;
    private final IRayTracer estimatedRayTracer = new DummyTracer();

    @Override
    public void onEnable() {

        SpeakerCreateListener.create();
        SpeakerDestroyListener.create();

        collector = new SpeakerCollector(this);

        initializeVersion();

        // load all apeakers
        for (Speaker speaker : databaseService.getRepository(Speaker.class).values()) {
            speaker.fixEnumSet(); // due to gson type guessing in storm
            registerSpeaker(speaker);
        }

        // setup garbage system
        new SpeakerGarbageCollection(this);

        // reset with new addon
        OpenAudioMc.getService(MediaService.class).getResetTriggers().add(() -> {
            speakerMediaMap.clear();
        });

        // tick redstone speakers
        if (StorageKey.SETTINGS_SPEAKER_REDSTONE_TICK_ENABLED.getBoolean()) {
            int interval = StorageKey.SETTINGS_SPEAKER_REDSTONE_TICK_INTERVAL.getInt();

            OpenAudioLogger.info("Starting redstone speaker tick task with interval " + interval + " ticks");

            new CustomPayloadOARunnable(() -> {
                for (Speaker speaker : speakerMap.values()) {
                    // does this speaker have a redstone trigger?
                    if (!ExtraSpeakerOptions.REQUIRES_REDSTONE.isEnabledFor(speaker)) return;

                    Location loc = speaker.getLocation().toLocation(OpenAudioFabric.getInstance().getServer());

                    // is the speakers chunk loaded?
                    World world = loc.getWorld();
                    if (world == null) continue;
                    if(world instanceof ServerWorld sw) {
                        if (!sw.isChunkLoaded(speaker.getLocation().getX() >> 4, speaker.getLocation().getZ() >> 4)) continue;
    
                        // is the speaker powered?
                        boolean poweredNow = sw.isReceivingRedstonePower(loc.getBlockPos());
                        boolean poweredBefore = speaker.isRedstonePowered();
    
                        // did it change?
                        if (poweredNow != poweredBefore) {
                            // update the speaker
                            speaker.setRedstonePowered(poweredNow);
                            if (ExtraSpeakerOptions.RESET_PLAYTHROUGH_ON_REDSTONE_LOSS.isEnabledFor(speaker)) speaker.getMedia().setStartInstant(System.currentTimeMillis());
    
                            // find nearby players
                            for (ServerPlayerEntity player : sw.getPlayers()) {
                                if (Location.locationFromEntity(player).distance(loc) > speaker.getRadius()) {
                                    continue;
                                }
    
                                SpigotConnection spigotConnection = OpenAudioMc.getService(SpigotPlayerService.class).getClient(player.getUuid());
                                if (spigotConnection != null) {
                                    spigotConnection.getSpeakerHandler().tick();
                                }
                            }
                        }
                    }
                }
            }).runTaskTimerAsync(interval, interval);
        } else {
            OpenAudioLogger.info("Redstone speaker tick task is disabled");
        }
    }

    public IRayTracer getRayTracer() {
        // provide a default ray tracer, just use the simple one for now
        return estimatedRayTracer;
    }

    private void initializeVersion() {
        //version = OpenAudioMc.getService(ServerService.class).getVersion();

        playerSkullItem = Items.PLAYER_HEAD;
        //playerSkullBlock = BlockState.PLAYER_HEAD;
    }

    public Speaker registerSpeaker(Speaker speaker) {
        if (speaker.getLocation() == null) {
            OpenAudioLogger.warn("Registering speaker with nil location " + speaker.getSpeakerId());
        }
        speakerMap.put(speaker.getLocation(), speaker);
        return speaker;
    }

    public Speaker getSpeaker(MappedLocation location) {
        return speakerMap.get(location);
    }

    public SpeakerMedia getMedia(String source) {
        if (speakerMediaMap.containsKey(source)) return speakerMediaMap.get(source);
        SpeakerMedia speakerMedia = new SpeakerMedia(source);
        speakerMediaMap.put(source, speakerMedia);
        return speakerMedia;
    }

    public void unlistSpeaker(MappedLocation location) {
        speakerMap.remove(location);
    }
}
