package com.craftmend.openaudiomc.bungee.modules.commands;

import com.craftmend.openaudiomc.api.clients.Client;
import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.proxy.interfaces.UserHooks;
import com.craftmend.openaudiomc.generic.user.User;
import com.craftmend.openaudiomc.generic.user.adapters.CommandSenderUserAdapter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.openaudiofabric.OpenAudioFabric;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

import java.util.Optional;

public class AudioCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        // .requires(s -> s.hasPermissionLevel(2))
        final LiteralCommandNode<ServerCommandSource> audioCommandNode = dispatcher.register(literal("audio")
                .then(argument("bedrock_token", StringArgumentType.string()).executes(AudioCommand::bedrockArg))
                .then(argument("players", EntityArgumentType.players()).executes(AudioCommand::playerArg))
                .executes(AudioCommand::noArgs));
        dispatcher.register(literal("sound").redirect(audioCommandNode));
        dispatcher.register(literal("connect").redirect(audioCommandNode));
        dispatcher.register(literal("media").redirect(audioCommandNode));
        dispatcher.register(literal("muziek").redirect(audioCommandNode));
        dispatcher.register(literal("geluid").redirect(audioCommandNode));
        dispatcher.register(literal("vc").redirect(audioCommandNode));
        dispatcher.register(literal("voicechat").redirect(audioCommandNode));
        dispatcher.register(literal("voice").redirect(audioCommandNode));
    }

    private static int noArgs(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (context.getSource().getPlayer() != null) {
            ServerPlayerEntity player = context.getSource().getPlayer();

            User user = OpenAudioFabric.resolveDependency(UserHooks.class).byUuid(player.getUuid());

            OpenAudioFabric.getService(NetworkingService.class).getClient(player.getUuid()).getAuth().publishSessionUrl();
            return 1;
        } else {
            return sendNoInputError(context);
        }
    }

    private static int bedrockArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (context.getSource().getPlayer() != null) {
            ServerPlayerEntity player = context.getSource().getPlayer();

            User user = OpenAudioFabric.resolveDependency(UserHooks.class).byUuid(player.getUuid());

            OpenAudioFabric.getService(NetworkingService.class).getClient(user.getUniqueId()).getAuth().activateToken(
                    user,
                    StringArgumentType.getString(context, "bedrock_token"));
            return 1;
        } else {
            return sendNoInputError(context);
        }
    }

    private static int playerArg(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for(ServerPlayerEntity p : EntityArgumentType.getPlayers(context, "players"))
        {
            CommandSenderUserAdapter adapter = new CommandSenderUserAdapter(p);

            Optional<Client> client = adapter.findClient();
            client.ifPresent(value -> ((ClientConnection) value).getAuth().publishSessionUrl());
        }
        return 1;
    }

    private static int sendNoInputError(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        {
            context.getSource().sendFeedback(() -> {
                return Text.literal((MagicValue.COMMAND_PREFIX.get(String.class)
                        + "You must provide a player name OR selector to send trigger the URL"));
            }, false);
            return 0;
        }
    }
}
