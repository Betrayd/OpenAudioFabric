package com.craftmend.openaudiomc.spigot.modules.speakers.listeners;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.generic.database.DatabaseService;
import com.craftmend.openaudiomc.generic.environment.MagicValue;
import com.craftmend.openaudiomc.generic.utils.BlockStateChangedCallback;
import com.craftmend.openaudiomc.generic.utils.Location;
import com.craftmend.openaudiomc.spigot.modules.speakers.SpeakerService;
import com.craftmend.openaudiomc.spigot.modules.speakers.objects.MappedLocation;
import com.craftmend.openaudiomc.spigot.modules.speakers.objects.Speaker;
import com.craftmend.openaudiomc.spigot.modules.speakers.utils.SpeakerUtils;
import com.mojang.logging.LogUtils;

import lombok.AllArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@AllArgsConstructor
public class SpeakerDestroyListener {

    private SpeakerService speakerService;

    private static SpeakerDestroyListener singleton = null;

    private SpeakerDestroyListener()
    {
        BlockStateChangedCallback.EVENT.register((BlockPos pos, BlockState state, boolean moved, World world, CallbackInfoReturnable<BlockState> cir) -> {
            this.blockStateChanged(pos, state, moved, world, cir);
        });
    }

    public static SpeakerDestroyListener create() {
        if (SpeakerDestroyListener.singleton == null) {
            SpeakerDestroyListener.singleton = new SpeakerDestroyListener();
        } else {
            LogUtils.getLogger().warn("tried to create a new SpeakerCreateListener but one already exists!");
        }
        return SpeakerDestroyListener.singleton;
    }

    public void blockStateChanged(BlockPos pos, BlockState state, boolean moved, World world, CallbackInfoReturnable<BlockState> cir)
    {
        if(world instanceof ServerWorld serverWorld && SpeakerUtils.isSpeakerSkull(world.getBlockEntity(pos)))
        {
            MappedLocation location = new MappedLocation(new Location(serverWorld, pos.getX(), pos.getY(), pos.getZ()));
            Speaker speaker = speakerService.getSpeaker(location);
            if (speaker == null) return;

            speakerService.unlistSpeaker(location);

            //save to config
            OpenAudioMc.getService(DatabaseService.class).getRepository(Speaker.class).delete(speaker);
            for(ServerPlayerEntity p : serverWorld.getPlayers())
            {
                if(p.hasPermissionLevel(2))
                {
                    p.sendMessage(Text.literal(MagicValue.COMMAND_PREFIX.get(String.class) + "\u00A7c" + "Speaker destroyed at " + pos.toString()));
                }
            }
        }   
    }

    /*
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();
        if (SpeakerUtils.isSpeakerSkull(broken)) {
            if (!isAllowed(event.getPlayer())) {
                event.getPlayer().sendMessage(MagicValue.COMMAND_PREFIX.get(String.class) + "You are not allowed to break OpenAudioMc speakers, please ask the server administrator for more information.");
                event.setCancelled(true);
                return;
            }

            MappedLocation location = new MappedLocation(broken.getLocation());
            Speaker speaker = speakerService.getSpeaker(location);
            if (speaker == null) return;

            speakerService.unlistSpeaker(location);

            //save to config
            OpenAudioMc.getService(DatabaseService.class).getRepository(Speaker.class).delete(speaker);

            event.getPlayer().sendMessage(MagicValue.COMMAND_PREFIX.get(String.class) + ChatColor.RED + "Speaker destroyed");

            event.getBlock().getWorld().dropItem(
                    event.getBlock().getLocation(),
                    SpeakerUtils.getSkull(speaker.getSource(), speaker.getRadius())
            );

            try {
                event.setDropItems(false);
            } catch (Exception ignored) {}
        }
    }

    private boolean isAllowed(PlayerEntity player) {
        return player.hasPermissionLevel(2);
        //        || player.hasPermission("openaudiomc.speakers.*")
        //        || player.hasPermission("openaudiomc.*")
        //        || player.hasPermission("openaudiomc.speakers.destroy");
    }*/

}
