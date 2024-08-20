package com.openaudiofabric.modules.platform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.craftmend.openaudiomc.generic.logging.OpenAudioLogger;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.craftmend.openaudiomc.generic.proxy.messages.PacketPlayer;
import com.craftmend.openaudiomc.generic.proxy.messages.StandardPacket;
import com.craftmend.openaudiomc.generic.proxy.models.ProxyNode;
import com.craftmend.openaudiomc.generic.user.User;
import com.craftmend.openaudiomc.generic.user.adapters.CommandSenderUserAdapter;
import com.craftmend.openaudiomc.generic.utils.CommandSender;
import com.craftmend.openaudiomc.generic.utils.FabricUtils;
import com.openaudiofabric.OpenAudioFabric;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class FabricUserHooks implements UserHooks {

    @Override
    public Collection<ProxyNode> getNodes() {
        return new ArrayList<>();
    }

    @Override
    public Collection<User<?>> getOnlineUsers() {
        List<User<?>> users = new ArrayList<>();
        for (PlayerEntity player : FabricUtils.currentServer.getPlayerManager().getPlayerList()) {
            users.add(new CommandSenderUserAdapter(player));
        }
        return users;
    }

    @Override
    public void sendPacket(User<?> user, StandardPacket packet) {
        if(user.getOriginal() instanceof ServerPlayerEntity player)
        {
            OpenAudioFabric.getInstance().getMessageReceiver().sendPacket(new PacketPlayer(player), packet);
        }
        else
        {
            OpenAudioLogger.warn("a user that was supposed to be sent a packet was somehow not a serverPlayerEntity");
        }
    }

    @Override
    public User<?> byUuid(UUID uuid) {
        PlayerEntity player = FabricUtils.currentServer.getPlayerManager().getPlayer(uuid);
        if(player != null)
        {
            return new CommandSenderUserAdapter(player);
        }
        return null;
    }

    @Override
    public User<?> fromCommandSender(CommandSender commandSender) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromCommandSender' should not exist in fabric");
    }
    
}
