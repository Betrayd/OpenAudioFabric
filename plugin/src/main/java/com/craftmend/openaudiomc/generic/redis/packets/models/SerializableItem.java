package com.craftmend.openaudiomc.generic.redis.packets.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import net.minecraft.item.Item;
// import org.bukkit.Material;
// import org.bukkit.inventory.ItemStack;
import net.minecraft.item.ItemStack;

@Builder(toBuilder = true)
@AllArgsConstructor
public class SerializableItem {

    private short durability;
    private Item item;

    @Deprecated
    public ItemStack toBukkit() {
        return toFabric();
    }

    public ItemStack toFabric() {
        return new ItemStack(item, durability);
    }

    public static SerializableItem fromBukkit(ItemStack itemStack) {
        return SerializableItem.builder()
                .item(itemStack.getItem())
                .durability((short) itemStack.getDamage())
                .build();
    }

}
