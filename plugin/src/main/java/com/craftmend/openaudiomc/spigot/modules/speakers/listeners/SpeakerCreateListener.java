package com.craftmend.openaudiomc.spigot.modules.speakers.listeners;

import java.util.EnumSet;
import java.util.UUID;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.api.speakers.ExtraSpeakerOptions;
import com.craftmend.openaudiomc.api.speakers.SpeakerType;
import com.craftmend.openaudiomc.generic.database.DatabaseService;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.utils.BlockPlaceCallback;
import com.craftmend.openaudiomc.generic.utils.Location;
import com.craftmend.openaudiomc.spigot.modules.speakers.SpeakerService;
import com.craftmend.openaudiomc.spigot.modules.speakers.objects.MappedLocation;
import com.craftmend.openaudiomc.spigot.modules.speakers.objects.Speaker;
import com.craftmend.openaudiomc.spigot.modules.speakers.utils.SpeakerUtils;
import com.mojang.logging.LogUtils;
import com.openaudiofabric.OpenAudioFabric;

import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

@AllArgsConstructor
public class SpeakerCreateListener {

    private SpeakerService speakerService;

    private static SpeakerCreateListener singleton = null;

    private SpeakerCreateListener()
    {
        BlockPlaceCallback.EVENT.register((context, state) -> {
            return this.onBlockPlace(context, state);
        });
    }

    public static SpeakerCreateListener create() {
        if (SpeakerCreateListener.singleton == null) {
            SpeakerCreateListener.singleton = new SpeakerCreateListener();
        } else {
            LogUtils.getLogger().warn("tried to create a new SpeakerCreateListener but one already exists! Passing old...");
        }
        return SpeakerCreateListener.singleton;
    }

    public ActionResult onBlockPlace(ItemPlacementContext context, BlockState state) {
        PlayerEntity player = context.getPlayer();
        if (player == null || context.getBlockPos() == null || context.getHand() == null) {
            return ActionResult.PASS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (SpeakerUtils.isSpeakerSkull(serverPlayer.getServerWorld().getBlockEntity(context.getBlockPos()))) {
                if (!isAllowed(serverPlayer)) {
                    serverPlayer.sendMessage(Text.literal(MagicValue.COMMAND_PREFIX.get(String.class) + "You are not allowed to place OpenAudioMc speakers, please ask the server administrator for more information."));
                    return ActionResult.FAIL;
                }

                NbtCompound nbti = serverPlayer.getStackInHand(context.getHand()).getNbt();
                String src = nbti.getString("oa-src");
                Integer radius = nbti.getInt("oa-radius");

                if (src == null) {
                    serverPlayer.sendMessage(Text.literal(MagicValue.COMMAND_PREFIX.get(String.class) + "This speaker seems to be invalid. Please spawn a new one."));
                    return ActionResult.FAIL;
                }

                UUID id = UUID.randomUUID();
                MappedLocation location = new MappedLocation(new Location(serverPlayer.getServerWorld(), context.getBlockPos().getX(), context.getBlockPos().getY(), context.getBlockPos().getZ()));

                SpeakerType speakerType = speakerService.getCollector().guessSpeakerType(OpenAudioFabric.getInstance().getServer(), location.toLocation(OpenAudioFabric.getInstance().getServer()), src);
                Speaker speaker = new Speaker(src, id, radius, location, speakerType,
                        EnumSet.noneOf(ExtraSpeakerOptions.class));
                speakerService.registerSpeaker(speaker);

                // save
                OpenAudioMc.getService(DatabaseService.class)
                        .getRepository(Speaker.class)
                        .save(speaker);

                serverPlayer
                        .sendMessage(Text.literal(MagicValue.COMMAND_PREFIX.get(String.class) + "\u00A72" + "Placed a "
                        + speakerType.getName() + " speaker" + "\u00A77"
                        + " (guessed bases on other nearby speakers, click placed speaker to edit)"));
                return ActionResult.PASS;
            }
        }
        return ActionResult.PASS;
    }

    private boolean isAllowed(PlayerEntity player) {
        return player.hasPermissionLevel(2);
        // || player.hasPermission("openaudiomc.speakers.*")
        // || player.hasPermission("openaudiomc.*")
        // || player.hasPermission("openaudiomc.speakers.create");
    }

}
