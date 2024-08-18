package com.craftmend.openaudiomc.spigot.modules.commands.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.platform.OaColor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.openaudiofabric.OpenAudioFabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;

public class ViewClientsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        // .requires(s -> s.hasPermissionLevel(2))
        final LiteralCommandNode<ServerCommandSource> clientsCommandNode = dispatcher.register(literal("clients").executes(ViewClientsCommand::sendConnectedClients));
    }

    private static int sendConnectedClients(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        context.getSource().sendFeedback(() -> {return Text.literal("Connected clients:");}, false);
        int count = 0;
        for (ClientConnection client :  NetworkingService.class.cast(OpenAudioFabric.getInstance().getServiceManager().loadService(NetworkingService.class)).getClients()) {
            if (client.isConnected()) {
                count++;
                String line = OaColor.AQUA + " - " + client.getUser().getName();
                // do they have voicechat?
                if (client.isMicrophoneActive()) {
                    line += OaColor.GRAY + " (Voicechat)";
                }

                final String finLine = line;
                // message them
                context.getSource().sendFeedback(() -> {return Text.literal(finLine);}, false);
            }
        }

        if (count == 0) {
            context.getSource().sendFeedback(() -> {return Text.literal("No clients connected");}, false);
        }
        context.getSource().sendFeedback(() -> {
            return Text.literal(MagicValue.COMMAND_PREFIX.get(String.class) + "OpenAudioFabric version "
                    + FabricLoader.getInstance().getModContainer(OpenAudioFabric.modID).get().getMetadata().getVersion()
                    + ". For help, please use /openaudio help");
        }, false);
        return 1;


    }
}
