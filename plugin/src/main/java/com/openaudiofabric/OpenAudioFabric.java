package com.openaudiofabric;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.networking.DefaultNetworkingService;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.platform.Platform;
import com.craftmend.openaudiomc.generic.platform.interfaces.OpenAudioInvoker;
import com.craftmend.openaudiomc.generic.platform.interfaces.TaskService;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.craftmend.openaudiomc.generic.proxy.messages.implementations.FabricPacketManager;
import com.craftmend.openaudiomc.generic.rd.RestDirectService;
import com.craftmend.openaudiomc.generic.state.StateService;
import com.craftmend.openaudiomc.generic.state.states.IdleState;
import com.craftmend.openaudiomc.generic.storage.interfaces.Configuration;
import com.craftmend.openaudiomc.generic.utils.FabricUtils;
import com.openaudiofabric.commands.AudioCommand;
import com.openaudiofabric.commands.VolumeCommand;
import com.openaudiofabric.modules.configuration.FabricConfiguration;
import com.openaudiofabric.modules.platform.FabricUserHooks;
import com.openaudiofabric.modules.player.PlayerConnectionListener;
import com.openaudiofabric.modules.scheduling.FabricTaskService;

import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class OpenAudioFabric implements ModInitializer, OpenAudioInvoker {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("open-audio-fabric");

	private static OpenAudioFabric instance;

	public static final String modID = "";

	public static OpenAudioFabric getInstance() {
		return instance;
	}

	@Getter private final File dataDir;

	private Instant boot = Instant.now();
	@Getter private FabricPacketManager messageReceiver;

	public OpenAudioFabric()
	{
        // TODO: implement proper config folder
		this.dataDir = FabricLoader.getInstance().getConfigDir().toFile();

        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new RuntimeException("Could not create data directory (" + dataDir + ")!");
        }
	}

    private WeakHashMap<MinecraftServer, FabricTaskService> taskServices = new WeakHashMap<>();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Starting OpenAudioFabric");

		instance = this;

        PlayerConnectionListener.register();

		CommandRegistrationCallback.EVENT.register(AudioCommand::register);
		CommandRegistrationCallback.EVENT.register(VolumeCommand::register);

		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer startedServer) -> 
		{
			FabricUtils.currentServer = startedServer;

            taskServices.put(startedServer, new FabricTaskService(startedServer));

			MagicValue.overWrite(MagicValue.STORAGE_DIRECTORY, this.dataDir);

				try {
					Instant.now();

					new OpenAudioMc(this);
					//add fabric of this
            		//getServer().getEventManager().register(this, new PlayerConnectionListener());

					//remove this one prolly
            		//this.commandModule = new VelocityCommandModule(this);
			
					this.messageReceiver = new FabricPacketManager("openaudiomc:node");


            		OpenAudioMc.getService(RestDirectService.class).boot();

           			// set state to idle, to allow connections and such
            		OpenAudioMc.getService(StateService.class).setState(new IdleState("OpenAudioMc started and awaiting command"));

            		// timing end and calc
            		Instant finish = Instant.now();
            		OpenAudioLogger.info("Starting and loading took " + Duration.between(boot, finish).toMillis() + "MS");

            		//this.messageReceiver.registerListener(new CommandPacketListener());

            		OpenAudioMc.getInstance().postBoot();
				}
				catch (Exception e) {
					OpenAudioLogger.error(e, "Failed to start OpenAudioMc");
				}
		});

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            FabricTaskService taskService = this.taskServices.get(server);
            if (taskService != null) {
                taskService.shutdown();
                this.taskServices.remove(server);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            FabricTaskService taskService = this.taskServices.get(server);
            if (taskService != null) {
                taskService.tickTasks();
            }
        });
	}

	@Override
	public boolean hasPlayersOnline() {
		return FabricUtils.currentServer.getPlayerManager().getPlayerList().size() > 0;
	}

	@Override
	public boolean isNodeServer() {
		return false;
	}

	@Override
	public Platform getPlatform() {
		return Platform.VELOCITY;
	}

	@Override
	public Class<? extends NetworkingService> getServiceClass() {
		return DefaultNetworkingService.class;
	}

	@Override
	public TaskService getTaskProvider() {
		return taskServices.get(FabricUtils.currentServer);
	}

	@Override
	public Configuration getConfigurationProvider() {
		return new FabricConfiguration();
	}

	@Override
	public String getPluginVersion() {
		return FabricLoader.getInstance().getModContainer(OpenAudioFabric.modID).get().getMetadata().getVersion().toString();
	}

	@Override
	public int getServerPort() {
		return FabricUtils.currentServer.getServerPort();
	}

	@Override
	public UserHooks getUserHooks() {
		return new FabricUserHooks();
	}
}