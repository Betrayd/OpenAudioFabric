package com.openaudiofabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.craftmend.openaudiomc.generic.utils.FabricUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class OpenAudioFabric implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("open-audio-fabric");

    public static final String modID = "";

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FabricUtils.currentServer = server;
        });
    }
}