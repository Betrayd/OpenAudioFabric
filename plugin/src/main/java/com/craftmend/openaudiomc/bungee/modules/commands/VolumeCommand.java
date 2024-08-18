package com.craftmend.openaudiomc.bungee.modules.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.platform.Platform;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.openaudiofabric.OpenAudioFabric;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;

public class VolumeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        // .requires(s -> s.hasPermissionLevel(2))
        final LiteralCommandNode<ServerCommandSource> volumeCommandNode = dispatcher.register(literal("volume")
                .then(argument("percent", IntegerArgumentType.integer(0, 100)).executes(VolumeCommand::execute)));
        dispatcher.register(literal("setvolume").redirect(volumeCommandNode));
        dispatcher.register(literal("vol").redirect(volumeCommandNode));
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendFeedback(() -> {return Text.literal("This command can only be used by players");}, false);
            return 0;
        }

        ClientConnection clientConnection = OpenAudioFabric.getService(NetworkingService.class)
                .getClient(context.getSource().getPlayer().getUuid());

        if (!clientConnection.isConnected()) {
            context.getSource().sendFeedback(() -> {return Text.literal(Platform.translateColors(StorageKey.MESSAGE_CLIENT_VOLUME_INVALID.getString()));}, false);
            return 0;
        }

        try {
            int volume = IntegerArgumentType.getInteger(context, "percent");
            // check if in range, just in case I don't know what I'm doing
            if (volume >= 0 || volume <= 100) {
                clientConnection.setVolume(volume);
                return 1;
            }
        } catch (Exception e) {
            String message = Platform.translateColors(
                    OpenAudioFabric.getInstance().getConfiguration().getString(StorageKey.MESSAGE_CLIENT_VOLUME_INVALID));
            context.getSource().sendFeedback(() -> {return Text.literal(message);}, false);
        }
        return 0;
    }
}
