package com.craftmend.openaudiomc.spigot.modules.show.runnables;

import com.craftmend.openaudiomc.generic.redis.packets.ExecuteCommandPacket;
import com.craftmend.openaudiomc.generic.utils.FabricUtils;
import com.craftmend.openaudiomc.spigot.modules.show.interfaces.ShowRunnable;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
@AllArgsConstructor
@NoArgsConstructor
    public class CommandRunnable extends ShowRunnable {

    private String command;
    private String worldName;

    @Override
    public void prepare(String serialized, World world) {
        this.command = serialized;
        this.worldName = FabricUtils.getWorldName(world);
        if (this.command.startsWith("/")) this.command = this.command.replace("/" , "");
    }

    @Override
    public String serialize() {
        return command;
    }

    @Override
    public void run() {
        if (!isExecutedFromRedis() && !command.toLowerCase().startsWith("oa show")) new ExecuteCommandPacket(command).send();

        ServerCommandSource commandSource = FabricUtils.currentServer.getCommandSource();
        if (worldName != null) {
            ServerWorld world = FabricUtils.getWorld(FabricUtils.currentServer, worldName);
            if (world == null)
                world = FabricUtils.currentServer.getOverworld();
            commandSource = commandSource.withWorld(world);
        }
        try {
            FabricUtils.currentServer.getCommandManager().getDispatcher().execute(command, commandSource);
        } catch (CommandSyntaxException e) {
            LogUtils.getLogger().error("Error executing command " + command, e);
        }

        // Bukkit.getScheduler().runTask(OpenAudioMcSpigot.getInstance(), () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command));

        /**
        if (worldName == null) {
            Bukkit.getScheduler().runTask(OpenAudioMcSpigot.getInstance(), () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command));
        } else {
            Entity executor = getExecutorEntity(worldName);

            if (executor == null) {
                throw new IllegalStateException("There is no entity loaded to execute the show trigger");
            }

            Bukkit.getScheduler().runTask(OpenAudioMcSpigot.getInstance(), () -> Bukkit.getServer().dispatchCommand(executor, command));
        }
         **/
    }
}
