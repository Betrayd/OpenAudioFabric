package com.craftmend.openaudiomc.spigot.modules.commands.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Locale;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.commands.objects.CommandError;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.spigot.modules.playlists.PlaylistService;
import com.craftmend.openaudiomc.spigot.modules.playlists.models.Playlist;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
                .then(literal("playlist")
                        .then(literal("create")
                                .then(argument("playlistName", StringArgumentType.string())
                                        .executes(OpenAudioMcCommand::playlistCreate)))
                        .then(literal("delete")
                                .then(argument("playlistName", StringArgumentType.string())
                                        .suggests(playlistsSuggestor())
                                        .executes(OpenAudioMcCommand::playlistDelete)))
                        .then(literal("list"))
                        .then(literal("view"))
                        .then(literal("add"))
                        .executes(OpenAudioMcCommand::sendVersion)));
        dispatcher.register(literal("oa").redirect(oaCommandNode));
        dispatcher.register(literal("oam").redirect(oaCommandNode));
        dispatcher.register(literal("openaudio").redirect(oaCommandNode));
    }

    public static SuggestionProvider<ServerCommandSource> playlistsSuggestor() {
        return (ctx, builder) -> {

            PlaylistService playlistService = OpenAudioMc.getService(PlaylistService.class);
            for (Playlist s : playlistService.getAll()) {
                builder.suggest(s.getName());
            }

            return builder.buildFuture();
        };
    }

    private static int sendVersion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(() -> {
            return Text.literal(MagicValue.COMMAND_PREFIX.get(String.class) + "OpenAudioFabric version "
                    + FabricLoader.getInstance().getModContainer(OpenAudioFabric.modID).get().getMetadata().getVersion()
                    + ". For help, please use /openaudio help");
        }, false);
        return 1;
    }

    private static PlaylistService playlistService = (PlaylistService.class)
            .cast(OpenAudioMc.getInstance().getServiceManager().loadService(PlaylistService.class));

    public static int playlistCreate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "playlist");

        if (playlistService.getPlaylist(name) != null) {
            context.getSource().sendError(Text.literal("A playlist with that name already exists"));
            return 0;
        }

        playlistService.createPlaylist(name, context.getSource().getName());
        (PlaylistService.class).cast(OpenAudioMc.getInstance().getServiceManager().loadService(PlaylistService.class))
                .saveAll();
        context.getSource().sendFeedback(() -> {
            return Text.literal("Created a new playlist with the name " + name);
        }, false);
        return 1;
    }

    private static int playlistDelete(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "playlist");

        if (playlistService.getPlaylist(name) == null) {
            context.getSource().sendError(Text.literal("A playlist with that name does not exist"));
            return 0;
        }

        playlistService.deletePlaylist(name);
        (PlaylistService.class).cast(OpenAudioMc.getInstance().getServiceManager().loadService(PlaylistService.class))
                .saveAll();
        context.getSource().sendFeedback(() -> {
            return Text.literal("Deleted the playlist with the name " + name);
        }, false);
        return 1;
    }
}
