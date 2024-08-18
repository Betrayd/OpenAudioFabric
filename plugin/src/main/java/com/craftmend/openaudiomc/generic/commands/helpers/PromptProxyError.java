package com.craftmend.openaudiomc.generic.commands.helpers;

import com.craftmend.openaudiomc.generic.platform.OaColor;
import com.craftmend.openaudiomc.generic.platform.Platform;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PromptProxyError {

    public static void sendTo(ServerCommandSource sender) {
        sender.sendFeedback(() -> {return Text.literal(OaColor.RED + "WARNING! This OpenAudioMc can't accept links, because it's running in node mode.");}, false);
        sender.sendFeedback(() -> {return Text.literal(OaColor.YELLOW + "If you run a proxy (Bunguard, Velocity, Waterfall, etc), then:");}, false);
        sender.sendFeedback(() -> {return Text.literal(OaColor.GRAY + " - Install the plugin on your proxy, if you have one.");}, false);
        sender.sendFeedback(() -> {return Text.literal(OaColor.YELLOW + "Or, if you don't run one or don't know what this means:");}, false);
        sender.sendFeedback(() -> {return Text.literal(OaColor.GRAY + " - Enable " + Platform.makeColor("WHITE") + "force-offline-mode" + Platform.makeColor("RED") + " in the config.yml if your host doesn't support proxies.");}, false);
        /*sender.sendClickableCommandMessage(
                Platform.makeColor("RED") + " - Or click here to do it automatically, but you need to restart your server after doing this.",
                "Automatically enable force-offline-mode",
                "oa setkv SETTINGS_FORCE_OFFLINE_MODE true"
        );*/
    }

}
