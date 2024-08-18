package com.craftmend.openaudiomc.spigot.modules.players.listeners;

import com.craftmend.openaudiomc.spigot.modules.players.SpigotPlayerService;
import com.openaudiofabric.OpenAudioFabric;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        OpenAudioFabric.getService(SpigotPlayerService.class).register(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        OpenAudioFabric.getService(SpigotPlayerService.class).remove(event.getPlayer());
    }

}
