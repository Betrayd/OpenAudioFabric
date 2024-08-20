package com.openaudiofabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.platform.Platform;
import com.craftmend.openaudiomc.generic.platform.interfaces.OpenAudioInvoker;
import com.craftmend.openaudiomc.generic.platform.interfaces.TaskService;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.craftmend.openaudiomc.generic.proxy.messages.implementations.VelocityPacketManager;
import com.craftmend.openaudiomc.generic.rd.RestDirectService;
import com.craftmend.openaudiomc.generic.state.StateService;
import com.craftmend.openaudiomc.generic.state.states.IdleState;
import com.craftmend.openaudiomc.generic.storage.interfaces.Configuration;
import com.craftmend.openaudiomc.spigot.modules.proxy.ProxyModule;
import com.craftmend.openaudiomc.velocity.modules.commands.VelocityCommandModule;
import com.craftmend.openaudiomc.velocity.modules.player.listeners.PlayerConnectionListener;
import com.craftmend.openaudiomc.velocity.platform.CommandPacketListener;
import com.craftmend.openaudiomc.velocity.platform.VelocityUserHooks;

import lombok.Getter;

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

	@Getter
	private MinecraftServer server;

	private final Instant boot = Instant.now();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Starting OpenAudioFabric");

		instance = this;
		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer startedServer) -> 
		{
			server = startedServer;

            new OpenAudioMc(this);

			//add fabric of this
            //getServer().getEventManager().register(this, new PlayerConnectionListener());

			//remove this one
            //this.commandModule = new VelocityCommandModule(this);
			
			//add fabric of this
            //this.messageReceiver = new VelocityPacketManager(this, getServer(),"openaudiomc:node");

            OpenAudioMc.getService(RestDirectService.class).boot();

            // set state to idle, to allow connections and such
            OpenAudioMc.getService(StateService.class).setState(new IdleState("OpenAudioMc started and awaiting command"));

            // timing end and calc
            Instant finish = Instant.now();
            OpenAudioLogger.info("Starting and loading took " + Duration.between(boot, finish).toMillis() + "MS");

            this.messageReceiver.registerListener(new CommandPacketListener());

            OpenAudioMc.getInstance().postBoot();
		});
	}

	@Override
	public boolean hasPlayersOnline() {
		return server.getPlayerManager().getPlayerList().size() > 0;
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getServiceClass'");
	}

	@Override
	public TaskService getTaskProvider() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTaskProvider'");
	}

	@Override
	public Configuration getConfigurationProvider() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getConfigurationProvider'");
	}

	@Override
	public String getPluginVersion() {
		return FabricLoader.getInstance().getModContainer(OpenAudioFabric.modID).get().getMetadata().getVersion().toString();
	}

	@Override
	public int getServerPort() {
		return server.getServerPort();
	}

	@Override
	public UserHooks getUserHooks() {
		return new VelocityUserHooks(this);
	}
}