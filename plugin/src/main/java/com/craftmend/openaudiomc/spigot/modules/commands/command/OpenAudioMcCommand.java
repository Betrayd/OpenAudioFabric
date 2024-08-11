package com.craftmend.openaudiomc.spigot.modules.commands.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.Locale;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.platform.OaColor;
import com.craftmend.openaudiomc.spigot.modules.playlists.PlaylistService;
import com.craftmend.openaudiomc.spigot.modules.playlists.models.Playlist;
import com.craftmend.openaudiomc.spigot.modules.playlists.models.PlaylistEntry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.openaudiofabric.OpenAudioFabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;

public class OpenAudioMcCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        // .requires(s -> s.hasPermissionLevel(2))
        final LiteralCommandNode<ServerCommandSource> oaCommandNode = dispatcher
                .register(literal("openaudiomc").requires(source -> source.hasPermissionLevel(2))
                        .then(literal("playlist")
                                .then(literal("create")
                                        .then(argument("playlist_name", StringArgumentType.string())
                                                .executes(OpenAudioMcCommand::playlistCreate)))
                                .then(literal("delete")
                                        .then(argument("playlist_name", StringArgumentType.string())
                                                .suggests(playlistsSuggestor())
                                                .executes(OpenAudioMcCommand::playlistDelete)))
                                .then(literal("list")
                                        .executes(OpenAudioMcCommand::playlistList))
                                .then(literal("remove")
                                        .then(argument("playlist_name", StringArgumentType.string())
                                                .suggests(playlistsSuggestor())
                                                .then(argument("track_index", IntegerArgumentType.integer())
                                                        .executes(OpenAudioMcCommand::playlistRemove))))
                                .then(literal("view")
                                        .then(argument("playlist_name", StringArgumentType.string())
                                                .suggests(playlistsSuggestor())
                                                .executes(OpenAudioMcCommand::playlistView)))
                                .then(literal("add")
                                        .then(argument("playlist_name", StringArgumentType.string())
                                        .suggests(playlistsSuggestor())
                                                .then(argument("source_url", StringArgumentType.string())
                                                        .executes(OpenAudioMcCommand::playlistAdd))))
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

    private static PlaylistService getPlaylistService()
    {
        return (PlaylistService.class).cast(OpenAudioMc.getInstance().getServiceManager().loadService(PlaylistService.class));
    }

    public static int playlistCreate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "playlist_name").toLowerCase(Locale.ROOT);

        if (playlistService.getPlaylist(name) != null) {
            context.getSource().sendError(Text.literal("A playlist with that name already exists"));
            return 0;
        }

        playlistService.createPlaylist(name, context.getSource().getName());
        getPlaylistService().saveAll();
        context.getSource().sendFeedback(() -> {
            return Text.literal("Created a new playlist with the name " + name);
        }, false);
        return 1;
    }

    private static int playlistDelete(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "playlist_name").toLowerCase(Locale.ROOT);

        if (playlistService.getPlaylist(name) == null) {
            context.getSource().sendError(Text.literal("A playlist with that name does not exist"));
            return 0;
        }

        playlistService.deletePlaylist(name);
        getPlaylistService().saveAll();
        context.getSource().sendFeedback(() -> {
            return Text.literal("Deleted the playlist with the name " + name);
        }, false);
        return 1;
    }

    private static int playlistList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<Playlist> playlists = playlistService.getAll();

        if (playlists.isEmpty()) {
            context.getSource().sendError(Text.literal("There are no playlists"));
            return 0;
        }

        context.getSource().sendFeedback(() -> {
            return Text.literal("There are " + playlists.size() + " playlists");
        }, false);
        for (Playlist playlist : playlists) {
            context.getSource().sendFeedback(() -> {
                return Text.literal(" - " + playlist.getName() + " by " + playlist.getCreatedBy());
            }, false);
        }
        return 1;
    }

    private static int playlistRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Playlist playlist = playlistService.getPlaylist(StringArgumentType.getString(context, "playlist_name").toLowerCase(Locale.ROOT));
        Integer index = IntegerArgumentType.getInteger(context, "track_index");

        if (playlist == null) {
            context.getSource().sendError(Text.literal("A playlist with that name does not exist"));
            return 0;
        }

        boolean removed = playlist.removeEntryAt(index);
        if (!removed) {
            context.getSource().sendError(Text.literal("There is no track at that index"));
            return 0;
        }
        getPlaylistService().saveAll();
        context.getSource().sendFeedback(() -> {return Text.literal("Removed track at index " + index + " from playlist " + playlist.getName());}, false);
        return 1;
    }

    private static int playlistView(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Playlist playlist = playlistService.getPlaylist(StringArgumentType.getString(context, "playlist_name").toLowerCase(Locale.ROOT));

        if (playlist == null) {
            context.getSource().sendError(Text.literal("A playlist with that name does not exist"));
            return 0;
        }

        if (playlist.getEntries().isEmpty()) {
            context.getSource().sendError(Text.literal("Playlist " + playlist.getName() + " by " + playlist.getCreatedBy() + " is empty"));
            return 0;
        }

        context.getSource().sendFeedback(() -> {return Text.literal("Playlist " + playlist.getName() + " by " + playlist.getCreatedBy() + " has " + playlist.getEntries().size() + " tracks");}, false);
        for (PlaylistEntry orderedEntry : playlist.getOrderedEntries()) {
            context.getSource().sendFeedback(() -> {return Text.literal(OaColor.RED + " - " + OaColor.DARK_RED + orderedEntry.getIndex() + OaColor.AQUA + " " + orderedEntry.getMedia()).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "openaudio playlist remove " + playlist.getName() + " " + orderedEntry.getIndex())));}, false);
        }

        context.getSource().sendFeedback(() -> {return Text.literal("You can click on a track to remove it from the playlist");}, false); 
        return 1;
    }

    private static int playlistAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Playlist playlist = playlistService.getPlaylist(StringArgumentType.getString(context, "playlist_name").toLowerCase(Locale.ROOT));
        String source = StringArgumentType.getString(context, "source_url");

        if (playlist == null) {
            context.getSource().sendError(Text.literal("A playlist with that name does not exist"));
            return 1;
        }

        playlist.addEntry(new PlaylistEntry(source));

        getPlaylistService().saveAll();
        context.getSource().sendFeedback(() -> {return Text.literal("Added " + source + " to playlist " + playlist.getName());}, false);
        return 1;
    }
}
