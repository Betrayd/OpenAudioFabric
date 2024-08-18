package com.craftmend.openaudiomc.bungee.modules.commands.subcommand;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.node.enums.ProxiedCommand;
import com.craftmend.openaudiomc.generic.node.packets.CommandProxyPacket;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.craftmend.openaudiomc.generic.user.User;
import com.craftmend.openaudiomc.spigot.modules.proxy.objects.CommandProxyPayload;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

public class AliasCommand {

    public String commandName = "Alias";

    public String helpType = "<alias name> <source>";
    public String helpText = "Register a Alias for a source URL so you can easaly memorize them and can paste them onto signs without having to type a complete dictionary." + " When an alias like onride_music is set, you can trigger it by using a:onride_music as your source.";

        public void onExecute(CommandContext<ServerCommandSource> context) {

        if (context.getSource().getPlayer() != null) {

            String[] args = {StringArgumentType.getString(context, ""), };
            CommandProxyPayload payload = new CommandProxyPayload();
            payload.setExecutor(context.getSource().getPlayer().getUuid());
            payload.setArgs(args);
            payload.setProxiedCommand(ProxiedCommand.ALIAS);

            context.getSource().getPlayer().networkHandler.sendPacket();
        }
    }
}
