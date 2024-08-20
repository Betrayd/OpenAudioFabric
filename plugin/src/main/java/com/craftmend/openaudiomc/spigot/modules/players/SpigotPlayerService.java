package com.craftmend.openaudiomc.spigot.modules.players;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.craftmend.openaudiomc.generic.service.Inject;
import com.craftmend.openaudiomc.generic.service.Service;
import com.craftmend.openaudiomc.generic.user.User;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.modules.players.listeners.PlayerConnectionListener;
import com.craftmend.openaudiomc.spigot.modules.players.listeners.PlayerTeleportationListener;
import com.craftmend.openaudiomc.spigot.modules.players.objects.SpigotConnection;
import com.openaudiofabric.OpenAudioFabric;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
public class SpigotPlayerService extends Service {

    @Inject
    private OpenAudioMcSpigot openAudioMcSpigot;

    @Getter
    private final Map<UUID, SpigotConnection> spigotConnectionMap = new HashMap<>();
    @Getter private PlayerConnectionListener playerConnectionListener;

    @Override
    public void onEnable() {
        playerConnectionListener = PlayerConnectionListener.create();
        PlayerTeleportationListener.create();

        //removed mic mute for security reasons so swap hands is no longer needed
        /*if (getService(ServerService.class).getVersion() == ServerVersion.MODERN) {
            openAudioMcSpigot.getServer().getPluginManager().registerEvents(new PlayerItemListener(), openAudioMcSpigot);
        }*/
    }

    @Override
    public void onDisable() {
        // loop over all clients
        for (SpigotConnection spigotConnection : spigotConnectionMap.values()) {
            // destroy them
            spigotConnection.onDestroy();
            spigotConnection.getClientConnection().kickConnection();
        }
    }

    /**
     * @param player registers the player
     */
    public void register(PlayerEntity player) {
        User sua = OpenAudioMc.resolveDependency(UserHooks.class).byUuid(player.getUuid());
        ClientConnection clientConnection = OpenAudioMc.getService(NetworkingService.class).register(sua, null);
        spigotConnectionMap.put(player.getUuid(), new SpigotConnection(player, clientConnection));
    }

    /**
     * @param uuid the uuid of a player
     * @return the client that corresponds to the player. can be null
     * used to work for offline. No longer does if that is relevant
     */
    public SpigotConnection getClient(UUID uuid) {
        SpigotConnection proposedSpigotConnection = spigotConnectionMap.get(uuid);

        if (proposedSpigotConnection != null) return proposedSpigotConnection;

        // check if the player is real
        PlayerEntity target = OpenAudioFabric.getInstance().getServer().getPlayerManager().getPlayer(uuid);
        if (target != null) {
            register(target);
            return getClient(uuid);
        }

        return null;
    }

    /**
     * @return a collection of all clients
     */
    public Collection<SpigotConnection> getClients() {
        return spigotConnectionMap.values();
    }

    /**
     * @param player target player
     * @return the connection of the player
     */
    public SpigotConnection getClient(PlayerEntity player) {
        return getClient(player.getUuid());
    }

    /**
     * @param player the player to unregister
     */
    public void remove(PlayerEntity player) {
        if (spigotConnectionMap.containsKey(player.getUuid())) {
            SpigotConnection spigotConnection = spigotConnectionMap.get(player.getUuid());
            spigotConnection.onDestroy();
            spigotConnectionMap.remove(player.getUuid());
        }

        OpenAudioMc.getService(NetworkingService.class).remove(player.getUuid());
    }

    public boolean hasClient(PlayerEntity player) {
        return spigotConnectionMap.containsKey(player.getUuid());
    }
}
