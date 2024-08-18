package com.craftmend.openaudiomc.spigot.modules.placeholderapi;

import com.craftmend.openaudiomc.generic.authentication.AuthenticationService;
import com.craftmend.openaudiomc.generic.oac.OpenaudioAccountService;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import com.craftmend.openaudiomc.spigot.modules.players.SpigotPlayerService;
import com.craftmend.openaudiomc.spigot.modules.players.objects.SpigotConnection;
import com.openaudiofabric.OpenAudioFabric;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderModule extends PlaceholderExpansion {

    private final OpenAudioMcSpigot spigot;

    public PlaceholderModule(OpenAudioMcSpigot spigot) {
        this.spigot = spigot;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "oa";
    }

    @Override
    public @NotNull String getAuthor() {
        return spigot.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if(params.equals("is_connected")) {
            boolean b = OpenAudioFabric.getService(
                            SpigotPlayerService.class
                    ).getClient(player.getUniqueId())
                    .getClientConnection()
                    .isConnected();

            if(b)
                return StorageKey.SETTINGS_PAPI_CLIENT_CONNECTED.getString();

            return StorageKey.SETTINGS_PAPI_CLIENT_DISCONNECTED.getString();
        }

        if(params.equals("is_in_voicechat")) {
            boolean b = OpenAudioFabric.getService(
                            SpigotPlayerService.class
                    ).getClient(player.getUniqueId())
                    .getClientConnection()
                    .getRtcSessionManager()
                    .isReady();

            if(b)
                return StorageKey.SETTINGS_PAPI_VC_CONNECTED.getString();

            return StorageKey.SETTINGS_PAPI_VC_DISCONNECTED.getString();
        }

        if(params.equals("client_count")) {
            int clients = 0;
            for (SpigotConnection spigotConnection : OpenAudioFabric.getService(SpigotPlayerService.class).getClients())
                if (spigotConnection.getClientConnection().isConnected())
                    clients++;

            return Integer.toString(clients);
        }

        if(params.equals("voicechat_peers")) {
            return Integer.toString(OpenAudioFabric.getService(
                            SpigotPlayerService.class
                    ).getClient(player.getUniqueId())
                    .getClientConnection().getRtcSessionManager().getCurrentProximityPeers().size());
        }

        if(params.equals("voicechat_count"))
            return Integer.toString(
                    OpenAudioFabric.getService(
                            OpenaudioAccountService.class
                    ).getVoiceApiConnection()
                            .getUsedSlots()
            );

        if(params.equals("voicechat_limit"))
            return Integer.toString(
                    OpenAudioFabric.getService(
                                    OpenaudioAccountService.class
                            ).getVoiceApiConnection()
                            .getMaxSlots()
            );

        if(params.equals("token")) {
            String token = OpenAudioFabric.getService(
                            AuthenticationService.class
                    ).getDriver()
                    .getSessionCacheMap()
                    .get(player.getUniqueId())
                    .getContext();

            if(token == null || token.isEmpty())
                return "/audio";

            return token;
        }

        return "invalid parameter";
    }

    @Override
    public boolean persist() {
        // persist between papi reloads, thank you Verum
        return true;
    }

}
