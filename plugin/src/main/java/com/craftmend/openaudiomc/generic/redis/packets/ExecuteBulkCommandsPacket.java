package com.craftmend.openaudiomc.generic.redis.packets;

import com.craftmend.openaudiomc.generic.redis.packets.interfaces.OARedisPacket;
import com.craftmend.openaudiomc.spigot.modules.show.runnables.CommandRunnable;
import com.openaudiofabric.OpenAudioFabric;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class ExecuteBulkCommandsPacket extends OARedisPacket {

    @Getter private List<String> commands;

    @Override
    public String serialize() {
        return OpenAudioFabric.getGson().toJson(this);
    }

    @Override
    public void handle(OARedisPacket a) {
        ExecuteBulkCommandsPacket received = (ExecuteBulkCommandsPacket) a;
        for (String command : received.getCommands()) {
            CommandRunnable commandRunnable = new CommandRunnable();
            commandRunnable.prepare(command, Bukkit.getWorlds().get(0));
            commandRunnable.setExecutedFromRedis(true);
            commandRunnable.run();
        }
    }
}
