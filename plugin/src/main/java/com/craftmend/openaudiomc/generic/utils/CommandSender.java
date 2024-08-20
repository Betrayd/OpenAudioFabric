package com.craftmend.openaudiomc.generic.utils;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;

/**
 * Simple re-implementation of Spigot's command sender
 */
public interface CommandSender {
        /**
     * Sends this sender a message
     *
     * @param message Message to be displayed
     */
    public void sendMessage(@NotNull String message);

    /**
     * Sends this sender multiple messages
     *
     * @param messages An array of messages to be displayed
     */
    public void sendMessage(@NotNull String... messages);

    /**
     * Sends this sender a message
     *
     * @param message Message to be displayed
     * @param sender The sender of this message
     */
    public void sendMessage(@Nullable UUID sender, @NotNull String message);

    /**
     * Sends this sender multiple messages
     *
     * @param messages An array of messages to be displayed
     * @param sender The sender of this message
     */
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages);

    /**
     * Returns the server instance that this command is running on
     *
     * @return Server instance
     */
    @NotNull
    public MinecraftServer getServer();

    /**
     * Gets the name of this command sender
     *
     * @return Name of the sender
     */
    @NotNull
    public String getName();
}
