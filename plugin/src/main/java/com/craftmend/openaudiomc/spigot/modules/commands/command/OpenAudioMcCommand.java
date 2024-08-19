package com.craftmend.openaudiomc.spigot.modules.commands.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;
import java.util.Locale;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.api.EventApi;
import com.craftmend.openaudiomc.api.events.client.SystemReloadEvent;
import com.craftmend.openaudiomc.generic.client.objects.ClientConnection;
import com.craftmend.openaudiomc.generic.commands.helpers.PromptProxyError;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.media.MediaService;
import com.craftmend.openaudiomc.generic.media.utils.Validation;
import com.craftmend.openaudiomc.generic.networking.interfaces.NetworkingService;
import com.craftmend.openaudiomc.generic.oac.OpenaudioAccountService;
import com.craftmend.openaudiomc.generic.platform.OaColor;
import com.craftmend.openaudiomc.generic.platform.Platform;
import com.craftmend.openaudiomc.generic.platform.interfaces.TaskService;
import com.craftmend.openaudiomc.generic.rest.RestRequest;
import com.craftmend.openaudiomc.generic.rest.routes.Endpoint;
import com.craftmend.openaudiomc.generic.rest.types.ClaimCodeResponse;
import com.craftmend.openaudiomc.generic.storage.enums.StorageKey;
import com.craftmend.openaudiomc.spigot.modules.playlists.PlaylistService;
import com.craftmend.openaudiomc.spigot.modules.playlists.models.Playlist;
import com.craftmend.openaudiomc.spigot.modules.playlists.models.PlaylistEntry;
import com.craftmend.openaudiomc.spigot.modules.speakers.tasks.SpeakerGarbageCollection;
import com.craftmend.openaudiomc.spigot.modules.speakers.utils.SpeakerUtils;
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
import net.minecraft.server.network.ServerPlayerEntity;
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
                                                        .executes(OpenAudioMcCommand::playlistAdd)))))
                        .then(literal("speaker")
                                .then(argument("source_url", StringArgumentType.string())
                                        .then(argument("radius", IntegerArgumentType.integer())
                                                .executes(ctx -> speakerGive(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "source_url"),
                                                        IntegerArgumentType.getInteger(ctx, "radius"))))
                                        .executes(ctx -> speakerGive(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "source_url"), 10)))
                                .then(literal("gc")
                                        .then(argument("confirm", StringArgumentType.string())
                                                .executes(OpenAudioMcCommand::speakerGarbageCollection))
                                        .executes(OpenAudioMcCommand::speakerGarbageCollectionWarn))
                                //the speaker commands I did not want to work on
                                /* .then(literal("set")
                                        .then(argument("x", IntegerArgumentType.integer())
                                                .then(argument("y", IntegerArgumentType.integer())
                                                        .then(argument("z", IntegerArgumentType.integer())
                                                                .then(argument("source_url", StringArgumentType.string())
                                                                        .then(argument("world", StringArgumentType.string())
                                                                                .executes(OpenAudioMcCommand::speakerSetCommandWorld))
                                                                        .executes(OpenAudioMcCommand::speakerSetCommandPlayer)))))) */)
                        .then(literal("link")
                                .executes(OpenAudioMcCommand::link))
                        .then(literal("reload").requires(source -> source.hasPermissionLevel(4))
                                .executes(OpenAudioMcCommand::reload))
                        .executes(OpenAudioMcCommand::sendVersion));
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
                    + OpenAudioFabric.getInstance().getPluginVersion()
                    + ". For help, please use /openaudio help");
        }, false);
        return 1;
    }

    private static PlaylistService playlistService = (PlaylistService.class)
            .cast(OpenAudioMc.getInstance().getServiceManager().loadService(PlaylistService.class));

    private static PlaylistService getPlaylistService() {
        return (PlaylistService.class)
                .cast(OpenAudioMc.getInstance().getServiceManager().loadService(PlaylistService.class));
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
        Playlist playlist = playlistService
                .getPlaylist(StringArgumentType.getString(context, "playlist_name").toLowerCase(Locale.ROOT));
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
        context.getSource().sendFeedback(() -> {
            return Text.literal("Removed track at index " + index + " from playlist " + playlist.getName());
        }, false);
        return 1;
    }

    private static int playlistView(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Playlist playlist = playlistService
                .getPlaylist(StringArgumentType.getString(context, "playlist_name").toLowerCase(Locale.ROOT));

        if (playlist == null) {
            context.getSource().sendError(Text.literal("A playlist with that name does not exist"));
            return 0;
        }

        if (playlist.getEntries().isEmpty()) {
            context.getSource().sendError(
                    Text.literal("Playlist " + playlist.getName() + " by " + playlist.getCreatedBy() + " is empty"));
            return 0;
        }

        context.getSource().sendFeedback(() -> {
            return Text.literal("Playlist " + playlist.getName() + " by " + playlist.getCreatedBy() + " has "
                    + playlist.getEntries().size() + " tracks");
        }, false);
        for (PlaylistEntry orderedEntry : playlist.getOrderedEntries()) {
            context.getSource().sendFeedback(() -> {
                return Text
                        .literal(OaColor.RED + " - " + OaColor.DARK_RED + orderedEntry.getIndex() + OaColor.AQUA + " "
                                + orderedEntry.getMedia())
                        .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "openaudio playlist remove " + playlist.getName() + " " + orderedEntry.getIndex())));
            }, false);
        }

        context.getSource().sendFeedback(() -> {
            return Text.literal("You can click on a track to remove it from the playlist");
        }, false);
        return 1;
    }

    private static int playlistAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Playlist playlist = playlistService
                .getPlaylist(StringArgumentType.getString(context, "playlist_name").toLowerCase(Locale.ROOT));
        String source = StringArgumentType.getString(context, "source_url");

        if (playlist == null) {
            context.getSource().sendError(Text.literal("A playlist with that name does not exist"));
            return 1;
        }

        playlist.addEntry(new PlaylistEntry(source));

        getPlaylistService().saveAll();
        context.getSource().sendFeedback(() -> {
            return Text.literal("Added " + source + " to playlist " + playlist.getName());
        }, false);
        return 1;
    }

    private static int speakerGive(ServerCommandSource sender, String source, int radius) {

        ServerPlayerEntity player = sender.getPlayer();
        if (player == null) {
            sender.sendError(Text.literal("Only players can receive a speaker item."));
            return 0;
        }

        if (radius > StorageKey.SETTINGS_SPEAKER_MAX_RANGE.getInt()) {
            sender.sendError(Text.literal("The radius is too large. The maximum radius is "
                    + StorageKey.SETTINGS_SPEAKER_MAX_RANGE.getInt()));
            return 0;
        }

        if (Validation.isStringInvalid(source)) {
            sender.sendError(Text.literal("Invalid source url."));
            return 0;
        }

        player.getInventory()
                .insertStack(SpeakerUtils.getSkull(OpenAudioMc.getService(MediaService.class).process(source), radius));
        sender.sendFeedback(() -> {
            return Text.literal(
                    "Speaker media created! You've received a Speaker skull in your inventory. Placing it anywhere in the world will add the configured sound in the are.");
        }, false);
        return 1;
    }

    private static int speakerGarbageCollectionWarn(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        context.getSource().sendFeedback(() -> {
            return Text.literal(OaColor.RED
                    + "WARNING! THIS COMMAND WILL LOAD AN INSANE AMOUNT OF CHUNKS IF YOU USE SPEAKERS THROUGHOUT YOUR SERVER. RUN "
                    + OaColor.YELLOW + "/oa speaker gc confirm" + OaColor.RED
                    + " IF YOU ARE 100% SURE THAT YOU WANT TO CONTINUE");
        }, false);
        return 1;
    }

    private static int speakerGarbageCollection(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (StringArgumentType.getString(context, "confirm").equalsIgnoreCase("confirm")) {
            return speakerGarbageCollectionWarn(context);
        }

        context.getSource().sendFeedback(() -> {
            return Text.literal("Starting garbage collector...");
        }, true);
        SpeakerGarbageCollection sgc = new SpeakerGarbageCollection();
        // run the wrapper twice to force a cache refresh at the end
        sgc.run();
        context.getSource().sendFeedback(() -> {
            return Text.literal("Full garbage collection sweep finished");
        }, true);
        return 1;
    }

    //this is all speaker stuff I don't want to work on

    /*private static int speakerSetCommandWorld(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String stringWorld = StringArgumentType.getString(context, "world");
        World world = context.getSource().getServer().getRegistryManager().get(RegistryKeys.WORLD).get(Identifier.tryParse(stringWorld));
        if(world != null && world instanceof ServerWorld sw)
        {
            return speakerSetCommand(context, sw);
        }
        context.getSource().sendError(Text.literal("could not parse world as Serverworld"));
        return 1;
    }

    private static int speakerSetCommandPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(context.getSource().getPlayer() != null)
        {
            return speakerSetCommand(context, context.getSource().getPlayer().getServerWorld());
        }
        context.getSource().sendError(Text.literal("This command must be executed by a player or contain a final <world> argument agter the source"));
        return 0;
    }

    private static int speakerSetCommand(CommandContext<ServerCommandSource> context, ServerWorld world) throws CommandSyntaxException {
        Integer x = IntegerArgumentType.getInteger(context, "x");
        Integer y = IntegerArgumentType.getInteger(context, "y");
        Integer z = IntegerArgumentType.getInteger(context, "z");
        String sourceURL = StringArgumentType.getString(context, "source_url");
        
        Location location = new Location(world, x, y, z);
        MappedLocation mappedLocation = new MappedLocation(location);

        String source = OpenAudioMc.getService(MediaService.class).process(sourceURL);

        // create
        UUID id = UUID.randomUUID();
        Configuration config = OpenAudioMc.getInstance().getConfiguration();
        int range = config.getInt(StorageKey.SETTINGS_SPEAKER_RANGE);

        SpeakerService speakerService = OpenAudioMc.getService(SpeakerService.class);

        // register
        Speaker speaker = new Speaker(source, id, range, mappedLocation, SpeakerService.DEFAULT_SPEAKER_TYPE, EnumSet.noneOf(ExtraSpeakerOptions.class));
        speakerService.registerSpeaker(speaker);
        // save
        OpenAudioMc.getService(DatabaseService.class)
                .getRepository(Speaker.class)
                .save(speaker);

        // place block
        world.breakBlock(location.getBlockPos(), false);


        Skull s = (Skull) location.getBlock().getState();

        if (OpenAudioMc.getService(ServerService.class).getVersion() == ServerVersion.LEGACY) {
            s.setSkullType(SkullType.PLAYER);
            // reflection for the old map
            try {
                Block.class.getMethod("setData", byte.class).invoke(location.getBlock(), (byte) 1);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                message(sender, "Something went wrong with reflection");
                e.printStackTrace();
                return;
            }

        } else {
            location.getBlock().setBlockData(OpenAudioMc.getService(SpeakerService.class).getPlayerSkullBlock().createBlockData());
        }
        s.setOwner(SpeakerUtils.speakerSkin);
        s.update();

        message(Text.literal("\u00A7a" + "Speaker placed"));
        return;
    }
        return 1;
    }*/

    private static int link(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (OpenAudioMc.getInstance().getInvoker().isNodeServer()) {
            PromptProxyError.sendTo(context.getSource());
            return 0;
        }

        // init connection so we receive the event
        OpenAudioMc.resolveDependency(TaskService.class)
                .runAsync(() -> OpenAudioMc.getService(NetworkingService.class).connectIfDown());

        context.getSource().sendFeedback(() -> {return Text.literal(OaColor.GRAY + "Generating link...");}, false);

        RestRequest<ClaimCodeResponse> request = new RestRequest<>(ClaimCodeResponse.class, Endpoint.CLAIM_CODE);

        request.runAsync()
                .thenAccept(state -> {
                    if (state.hasError()) {
                        context.getSource().sendFeedback(() -> {return Text.literal(OaColor.RED + state.getError().getMessage());}, false);
                        return;
                    }

                    ClaimCodeResponse response = state.getResponse();
                    String url = response.getClaimUrl();

                    context.getSource().sendFeedback(() -> {return Text.literal("Successfully generated a link for you to claim your account!");}, false);
                    final Text clickableMessage = Text.literal(OaColor.GOLD + " >> Click here to claim your account << ").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
                    context.getSource().sendFeedback(() -> {return clickableMessage;}, false);
                    context.getSource().sendFeedback(() -> {return Text.literal(OaColor.GRAY + " >> or visit " + url);}, false);
                });
        return 1;
    }

    private static int reload(CommandContext<ServerCommandSource> context) throws CommandSyntaxException 
    {
        context.getSource().sendFeedback(() -> {return Text.literal(Platform.makeColor("RED") + "Reloading OpenAudioMc data (config and account details)...");}, false);
        OpenAudioMc.getInstance().getConfiguration().reloadConfig();
        OpenAudioMc.getService(OpenaudioAccountService.class).syncAccount();

        context.getSource().sendFeedback(() -> {return Text.literal(Platform.makeColor("RED") + "Shutting down network service and logging out...");}, false);

        for (ClientConnection client : OpenAudioMc.getService(NetworkingService.class).getClients()) {
            client.kick(() -> {
            });
        }

        OpenAudioMc.getService(NetworkingService.class).stop();

        context.getSource().sendFeedback(() -> {return Text.literal(Platform.makeColor("RED") + "Re-activating account...");}, false);
        OpenAudioMc.resolveDependency(TaskService.class)
                .runAsync(() -> OpenAudioMc.getService(NetworkingService.class).connectIfDown());

        EventApi.getInstance().callEvent(new SystemReloadEvent());

        context.getSource().sendFeedback(() -> {return Text.literal(Platform.makeColor("GREEN") + "Reloaded system! Welcome back.");}, false);
        return 1;
    }

    //reload

}
