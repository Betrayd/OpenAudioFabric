package com.craftmend.openaudiomc.spigot.modules.speakers.utils;

import java.util.UUID;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.spigot.modules.speakers.SpeakerService;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SpeakerUtils {

    public static final String speakerSkin = "OpenAudioMc";
    public static final UUID speakerUUID = UUID.fromString("c0db149e-d498-4a16-8e35-93d57577589f");
    private static final SpeakerService SPEAKER_SERVICE = OpenAudioMc.getService(SpeakerService.class);

    public static boolean isSpeakerSkull(BlockEntity block) {
        if (block instanceof SkullBlockEntity skull) {
            return skull.getOwner().getId().equals(speakerUUID);
        }
        return false;
    }

    public static ItemStack getSkull(String source, int radius) {
        ItemStack skull = SPEAKER_SERVICE.getPlayerSkullItem().getDefaultStack();
        skull.setCustomName(Text.literal("OpenAudioMc Speaker").formatted(Formatting.AQUA));
        skull.setDamage(3);
        NbtCompound sm = skull.getOrCreateNbt();
        if (sm != null) {
            sm.putUuid("SkullOwner", speakerUUID);

            NbtList lore = skull.getOrCreateSubNbt("display").getList("Lore", NbtElement.STRING_TYPE);
            lore.add(NbtString.of(Text.Serialization.toJsonString(Text.literal("I'm a super cool speaker!").formatted(Formatting.AQUA))));
            lore.add(NbtString.of(Text.Serialization.toJsonString(Text.literal("Simply place me in your world").formatted(Formatting.AQUA))));
            lore.add(NbtString.of(Text.Serialization.toJsonString(Text.literal("and I'll play your customized music").formatted(Formatting.AQUA))));
            lore.add(NbtString.of(Text.Serialization.toJsonString(Text.literal("").formatted(Formatting.AQUA))));
            lore.add(NbtString.of(Text.Serialization.toJsonString(Text.literal("SRC: ").formatted(Formatting.AQUA).append(Text.literal(source).formatted(Formatting.GREEN)))));
            lore.add(NbtString.of(Text.Serialization.toJsonString(Text.literal("Radius: ").formatted(Formatting.AQUA).append(Text.literal(""+radius).formatted(Formatting.GREEN)))));
            skull.getOrCreateSubNbt("display").put("Lore", lore);

            sm.putString("oa-src", source);
            sm.putInt("oa-radius", radius);
        }

        return skull;
    }


}
