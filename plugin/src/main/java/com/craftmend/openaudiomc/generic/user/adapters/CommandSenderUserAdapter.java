package com.craftmend.openaudiomc.generic.user.adapters;

import java.util.UUID;

import org.slf4j.LoggerFactory;

import com.craftmend.openaudiomc.generic.user.User;
import com.craftmend.openaudiomc.generic.utils.FabricUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lombok.AllArgsConstructor;
// import net.md_5.bungee.api.chat.TextComponent;
// import org.bukkit.Bukkit;
// import org.bukkit.command.CommandSender;
// import org.bukkit.entity.Player;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.Text;

@AllArgsConstructor
public class CommandSenderUserAdapter implements User<PlayerEntity> {

    private PlayerEntity sender;

    @Override
    public void sendMessage(String string) {
        for (String s : string.split("\\\\n")) {
            sender.sendMessage(Text.literal(s));
        }
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        // if (sender instanceof Player) {
        //     ((Player) sender).sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        // }
        // TODO: subtitle, etc
        if (sender instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(title)));
        }
    }

    @Override
    public void sendMessage(Text textComponent) {
        sender.sendMessage(textComponent);
        return;
    }

    @Override
    public void sendClickableCommandMessage(String t, String hoverMessage, String command) {
        sender.sendMessage(Text.literal(t)
                .styled(s -> s.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Text.literal(hoverMessage)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))));
    }

    @Override
    public void sendClickableUrlMessage(String t, String hoverMessage, String url) {
        sender.sendMessage(Text.literal(t)
                .styled(s -> s.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Text.literal(hoverMessage)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))));
    }

    @Override
    public boolean isAdministrator() {
        return sender.hasPermissionLevel(2);
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public void makeExecuteCommand(String command) {
        if (sender instanceof ServerPlayerEntity serverPlayer) {
            try {
                serverPlayer.getServer().getCommandManager().getDispatcher().execute(command, serverPlayer.getCommandSource());
            } catch (CommandSyntaxException e) {
                LoggerFactory.getLogger(getClass()).error("Error executing command.", e);
            }
        }
        // Bukkit.dispatchCommand(sender, command);
    }

    @Override
    public UUID getUniqueId() {
        return sender.getUuid();
    }

    @Override
    public String getIpAddress() {
        if (sender instanceof ServerPlayerEntity serverPlayer) {
            return serverPlayer.getIp();
        } else {
            return "localhost";
        }
    }

    @Override
    public PlayerEntity getOriginal() {
        return sender;
    }

    @Override
    public String getName() {
        return sender.getName().getString();
    }

    @Override
    public String getWorld() {
        return FabricUtils.getWorldName(sender.getWorld());

        // // player
        // if (sender instanceof Player) {
        //     return ((Player) sender).getWorld().getName();
        // }

        // // entity
        // if (sender instanceof org.bukkit.entity.Entity) {
        //     return ((org.bukkit.entity.Entity) sender).getWorld().getName();
        // }

        // // commandblock
        // if (sender instanceof org.bukkit.command.BlockCommandSender) {
        //     return ((org.bukkit.command.BlockCommandSender) sender).getBlock().getWorld().getName();
        // }

        // return StorageKey.SETTINGS_DEFAULT_WORLD_NAME.getString();
    }
}
