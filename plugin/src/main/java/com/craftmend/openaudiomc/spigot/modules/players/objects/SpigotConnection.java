package com.craftmend.openaudiomc.spigot.modules.players.objects;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.api.media.Media;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.networking.packets.client.speakers.PacketClientUpdateLocation;
import com.craftmend.openaudiomc.generic.networking.payloads.client.speakers.ClientPlayerLocationPayload;
import com.craftmend.openaudiomc.generic.platform.interfaces.TaskService;
import com.craftmend.openaudiomc.generic.utils.CustomPayloadOARunnable;
import com.craftmend.openaudiomc.generic.utils.Location;
import com.craftmend.openaudiomc.spigot.modules.players.enums.PlayerLocationFollower;
import com.craftmend.openaudiomc.spigot.modules.players.events.ClientDisconnectEvent;
import com.craftmend.openaudiomc.spigot.modules.players.handlers.AudioChunkHandler;
import com.craftmend.openaudiomc.spigot.modules.players.handlers.InitializeTrains;
import com.craftmend.openaudiomc.spigot.modules.players.handlers.RegionHandler;
import com.craftmend.openaudiomc.spigot.modules.players.handlers.SpeakerHandler;
import com.craftmend.openaudiomc.spigot.modules.players.events.ClientConnectEvent;
import com.craftmend.openaudiomc.spigot.modules.regions.interfaces.IRegion;
import com.craftmend.openaudiomc.spigot.modules.speakers.objects.ApplicableSpeaker;

import com.craftmend.openaudiomc.spigot.modules.speakers.objects.SpeakerSettings;
import com.craftmend.openaudiomc.spigot.modules.voicechat.VoiceChannelService;
import com.craftmend.openaudiomc.spigot.services.utils.DataWatcher;
import com.openaudiofabric.OpenAudioFabric;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.PlayerEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

public class SpigotConnection {

    @Getter
    private final ClientConnection clientConnection;

    // optional regions and speakers
    @Getter
    @Setter
    private List<IRegion> currentRegions = new ArrayList<>();
    @Setter
    private List<ApplicableSpeaker> currentSpeakers = new ArrayList<>();

    // data watcher that watches for changes in the location, every 2 ticks.
    @Getter
    private final DataWatcher<Location> locationDataWatcher = new DataWatcher<>(
            false,
            MagicValue.LOCATION_TRACK_INTERVAL.get(Integer.class)
    );

    // Speaker and region handles. Region handler can be null if the feature is disabled
    @Getter private final SpeakerHandler speakerHandler;
    @Getter private RegionHandler regionHandler;
    @Getter private final AudioChunkHandler audioChunkHandler;
    @Getter private final Set<PlayerLocationFollower> locationFollowers = new HashSet<>();
    @Getter private final PlayerEntity bukkitPlayer;
    @Setter @Getter private Instant lastVoiceReminderMessage = Instant.now();

    //plugin data
    @Setter
    @Getter
    private SpeakerSettings selectedSpeakerSettings = null;

    /**
     * @param player client startup logic
     */
    public SpigotConnection(PlayerEntity player, ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
        this.bukkitPlayer = player;
        // register the speaker handler
        this.speakerHandler = new SpeakerHandler(player, this);
        this.audioChunkHandler = new AudioChunkHandler(player, this);

        // code that fires when the location has been changed
        locationDataWatcher.setTask(updatedLocation -> {
            // if the client is not connected, then dont do shit, they wont hear it anyway
            if (!this.clientConnection.isConnected()) return;
            //keeping this comment because it's funny
            //if (!player.isOnline()) return; // how?.. what?..

            this.audioChunkHandler.tick();

            // tick the regions, if the regions are enabled
            if (this.regionHandler != null) this.regionHandler.tick();

            // tick the speakers to force them to update
            this.speakerHandler.tick();

            // update location if wanted
            tickLocationFollowers();
        });

        //if position is not getting updated then this is probably to blame
        // the feeder, how the data watcher gets its new fed data
        locationDataWatcher.setFeeder(() -> {return Location.locationFromEntity(player);});

        // set handlers
        clientConnection.addOnConnectHandler(() -> {
            audioChunkHandler.reset();
            currentRegions.clear();
            currentSpeakers.clear();

            locationDataWatcher.getCallback().accept(Location.locationFromEntity(player));
            
            //not sure when this is called. Will try running without it for now and look for issues
            //Bukkit.getScheduler().runTask(OpenAudioFabric.getInstance(), () -> Bukkit.getServer().getPluginManager().callEvent(new ClientConnectEvent(player, this)));
        });

        //clientConnection.addOnConnectHandler(new InitializeTrains(player));

        clientConnection.addOnDisconnectHandler(() -> {
            OpenAudioMc.resolveDependency(TaskService.class).runSync(() -> {
                Bukkit.getServer().getPluginManager().callEvent(new ClientDisconnectEvent(player));
            });

            OpenAudioMc.getService(VoiceChannelService.class).handleUserDisconnect(clientConnection);
        });
    }

    private void tickLocationFollowers() {
        if (!locationFollowers.isEmpty()) {
            Location location = bukkitPlayer.getLocation();
            ClientPlayerLocationPayload locationPayload = new ClientPlayerLocationPayload(
                    round(location.getX(), 1),
                    round(location.getY(), 1),
                    round(location.getZ(), 1),
                    (int) location.getPitch(),
                    (int) location.getYaw()
            );

            OpenAudioMc.getService(NetworkingService.class).send(getClientConnection(), new PacketClientUpdateLocation(locationPayload));

            if (locationFollowers.contains(PlayerLocationFollower.PROXIMITY_VOICE_CHAT)) {
                clientConnection.getRtcSessionManager().onLocationTick(location);
            }
        }
    }

    /**
     * Called before the Client object is destroyed
     */
    public void onDestroy() {
        // shutdown the data watcher
        this.locationDataWatcher.stop();
        this.currentSpeakers.clear();
        this.currentRegions.clear();
        OpenAudioMc.getService(VoiceChannelService.class).handleUserDisconnect(clientConnection);
    }

    /**
     * @return regions that the player is a part of
     */
    public List<IRegion> getRegions() {
        return currentRegions;
    }

    /**
     * @return speakers in range of the player
     */
    public List<ApplicableSpeaker> getSpeakers() {
        return currentSpeakers;
    }

    /**
     * @param media start media for the client
     */
    public void playMedia(Media media) {
        clientConnection.sendMedia(media);
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
