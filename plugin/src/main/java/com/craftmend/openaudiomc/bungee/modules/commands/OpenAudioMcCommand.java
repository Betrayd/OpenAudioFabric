package com.craftmend.openaudiomc.bungee.modules.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.openaudiofabric.OpenAudioFabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;

public class OpenAudioMcCommand {
        public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        // .requires(s -> s.hasPermissionLevel(2))
        final LiteralCommandNode<ServerCommandSource> oaCommandNode = dispatcher.register(literal("openaudiomc")
                .then(argument("percent", IntegerArgumentType.integer(0, 100)))
                .executes(OpenAudioMcCommand::sendVersion));
        dispatcher.register(literal("setvolume").redirect(oaCommandNode));
        dispatcher.register(literal("vol").redirect(oaCommandNode));
    }

    private static int sendVersion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(() -> {return Text.literal(MagicValue.COMMAND_PREFIX.get(String.class) + "OpenAudioFabric version " + FabricLoader.getInstance().getModContainer(OpenAudioFabric.modID).get().getMetadata().getVersion() + ". For help, please use /openaudio help");}, false);
        return 1;
    }

    
}
