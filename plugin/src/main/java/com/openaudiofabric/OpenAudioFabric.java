package com.openaudiofabric;

import java.time.Instant;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.api.impl.RegistryApiImpl;
import com.craftmend.openaudiomc.api.interfaces.AudioApi;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.platform.Platform;
import com.craftmend.openaudiomc.generic.platform.interfaces.OpenAudioInvoker;
import com.craftmend.openaudiomc.generic.platform.interfaces.TaskService;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.craftmend.openaudiomc.generic.rd.RestDirectService;
import com.craftmend.openaudiomc.generic.state.StateService;
import com.craftmend.openaudiomc.generic.state.states.IdleState;
import com.craftmend.openaudiomc.generic.state.states.WorkerState;
import com.craftmend.openaudiomc.generic.storage.interfaces.Configuration;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.modules.commands.SpigotCommandService;
import com.craftmend.openaudiomc.spigot.modules.configuration.SpigotConfiguration;
import com.craftmend.openaudiomc.spigot.modules.placeholderapi.service.PlaceholderService;
import com.craftmend.openaudiomc.spigot.modules.players.SpigotPlayerService;
import com.craftmend.openaudiomc.spigot.modules.playlists.PlaylistService;
import com.craftmend.openaudiomc.spigot.modules.predictive.PredictiveMediaService;
import com.craftmend.openaudiomc.spigot.modules.proxy.ProxyModule;
import com.craftmend.openaudiomc.spigot.modules.proxy.enums.OAClientMode;
import com.craftmend.openaudiomc.spigot.modules.punishments.EssentialsIntegration;
import com.craftmend.openaudiomc.spigot.modules.punishments.LitebansIntegration;
import com.craftmend.openaudiomc.spigot.modules.regions.RegionModule;
import com.craftmend.openaudiomc.spigot.modules.regions.service.RegionService;
import com.craftmend.openaudiomc.spigot.modules.rules.MediaRuleService;
import com.craftmend.openaudiomc.spigot.modules.shortner.AliasService;
import com.craftmend.openaudiomc.spigot.modules.show.ShowService;
import com.craftmend.openaudiomc.spigot.modules.speakers.SpeakerService;
import com.craftmend.openaudiomc.spigot.modules.traincarts.TrainCartsModule;
import com.craftmend.openaudiomc.spigot.modules.traincarts.service.TrainCartsService;
import com.craftmend.openaudiomc.spigot.modules.users.SpigotUserHooks;
import com.craftmend.openaudiomc.spigot.modules.voicechat.SpigotVoiceChatService;
import com.craftmend.openaudiomc.spigot.modules.voicechat.VoiceChannelService;
import com.craftmend.openaudiomc.spigot.modules.voicechat.filters.FilterService;
import com.craftmend.openaudiomc.spigot.services.scheduling.SpigotTaskService;
import com.craftmend.openaudiomc.spigot.services.server.ServerService;
import com.craftmend.openaudiomc.spigot.services.threading.ExecutorService;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class OpenAudioFabric implements ModInitializer, OpenAudioInvoker {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("open-audio-fabric");

	public static final String modID = "";

	private static OpenAudioFabric instance;

	public MinecraftServer server;

	//regions are not in OpenAudioFabric since worldguard does not exist
    //@Setter private RegionModule regionModule;
	//traincarts are not in OpenAudioFabric since I was too lazy to implement them
	//@Setter private TrainCartsModule trainCartsModule;
    private OpenAudioMc openAudioMc;
    private ProxyModule proxyModule;
	private boolean bound = false;

    private final Instant boot = Instant.now();


	public static OpenAudioFabric getInstance() {
		return instance;
	}

	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("registering openAudioFabric...");

		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer startedServer) -> {
			// startup logic
			OpenAudioLogger.info("Initializing OpenAudio...");
			this.server = startedServer;

			MagicValue.overWrite(MagicValue.STORAGE_DIRECTORY, getDataFolder()/* returns: file, Spigot for the file the plugin data is stored in. Probably the YML?*/);
			MagicValue.loadArguments();

			if (MagicValue.PLATFORM_FORCE_LATE_FIND.get(Boolean.class) != null && MagicValue.PLATFORM_FORCE_LATE_FIND.get(Boolean.class) && !bound) {
				OpenAudioLogger.warn("Using late bind! not doing anything for now...");
				bound = true;
				return;
			}

			if (MagicValue.PLATFORM_FORCE_LATE_FIND.get(Boolean.class) != null && MagicValue.PLATFORM_FORCE_LATE_FIND.get(Boolean.class) && !bound) {
				OpenAudioLogger.warn("Using late bind! not doing anything for now...");
				bound = true;
				return;
			}

			// setup core
			try {
				proxyModule = new ProxyModule();
				openAudioMc = new OpenAudioMc(this);
				openAudioMc.getServiceManager().registerDependency(ProxyModule.class, proxyModule);
				openAudioMc.getServiceManager().registerDependency(OpenAudioMcSpigot.class, this);

				// manually register the proxy module
				// it won't fully get registered because it gets manually injected
				// instead of being picked up by the service manager
				// causing an issue with dependencies (configuration)
				proxyModule.onEnable();

				openAudioMc.getServiceManager().loadServices(
						//remove this one since we no longer have plugins that load with it
						//SpigotDependencyService.class,
						//remove this since alias is removed because we decided that MC functions should be used instead if it matters or plenty of other alternatives
						//AliasService.class,
						ExecutorService.class,
						//removed ServerService because this is used for multiversion support, which is a bad idea in a dirrect fabric mod
						//ServerService.class,

						//MAY remove this if I can figure out how nessisary it is later and if we can repllace it with real MC stuff
						SpigotPlayerService.class,

						SpeakerService.class,
						SpigotCommandService.class,
						ShowService.class,
						PredictiveMediaService.class,
						SpigotVoiceChatService.class,
						VoiceChannelService.class,
						FilterService.class,
						MediaRuleService.class,
						PlaylistService.class);

				/*OpenAudioMc.getService(SpigotDependencyService.class)
						.ifPluginEnabled("LiteBans", new LitebansIntegration())
						.ifPluginEnabled("Essentials", new EssentialsIntegration())
						.ifPluginEnabled("WorldGuard", new RegionService(this))
						.ifPluginEnabled("Train_Carts", new TrainCartsService(this))
						.ifPluginEnabled("PlaceholderAPI", new PlaceholderService(this));*/

				// set state to idle, to allow connections and such, but only if not a node
				if (OpenAudioMc.getService(ProxyModule.class).getMode() == OAClientMode.NODE) {
					OpenAudioMc.getService(StateService.class).setState(new WorkerState());
				} else {
					OpenAudioMc.getService(RestDirectService.class).boot();
					OpenAudioMc.getService(StateService.class)
							.setState(new IdleState("OpenAudioMc started and awaiting command"));
				}

				// timing end and calc
				Instant finish = Instant.now();
				OpenAudioLogger.info("Starting and loading took " + Duration.between(boot, finish).toMillis() + "MS");

				OpenAudioMc.getInstance().postBoot();
			} catch (Exception e) {
				OpenAudioLogger.error(e,
						"A fatal error occurred while enabling OpenAudio.");
			}
		});
	}

	@Override
	public boolean hasPlayersOnline() {
		return server.getPlayerManager().getPlayerList().size() > 0;
	}

	@Override
	public boolean isNodeServer() {
		return proxyModule.getMode() != OAClientMode.STAND_ALONE;
	}

	@Override
	public Class<? extends NetworkingService> getServiceClass() {
		// check if there's a forced service
        Class<? extends NetworkingService> forced = ((RegistryApiImpl) AudioApi.getInstance().getRegistryApi()).getForcedService();
        if (forced != null) {
            OpenAudioLogger.warn("Using forced networking class " + forced.getName());
            return forced;
        }

        proxyModule.refresh();
        OpenAudioLogger.info("Using networking class " + proxyModule.getMode().getServiceClass().getName());
        return proxyModule.getMode().getServiceClass();
	}

	@Override
	public TaskService getTaskProvider() {
		return new SpigotTaskService();
	}

	@Override
	public Configuration getConfigurationProvider() {
		//Add FabricConfiguration here
		return new SpigotConfiguration(OpenAudioFabric.getInstance());
	}

	@Override
	public String getPluginVersion() {
		return FabricLoader.getInstance().getModContainer(OpenAudioFabric.modID).get().getMetadata().getVersion().toString();
	}

	@Override
	public int getServerPort() {
		server.getServerPort();
	}

	@Override
	public UserHooks getUserHooks() {
		return new SpigotUserHooks();
	}
}