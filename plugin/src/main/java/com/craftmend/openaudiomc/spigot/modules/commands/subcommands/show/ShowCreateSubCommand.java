package com.craftmend.openaudiomc.spigot.modules.commands.subcommands.show;

import com.craftmend.openaudiomc.generic.commands.interfaces.SubCommand;
import com.craftmend.openaudiomc.generic.user.User;
import com.craftmend.openaudiomc.spigot.modules.show.ShowService;
import com.openaudiofabric.OpenAudioFabric;

import org.bukkit.ChatColor;

public class ShowCreateSubCommand extends SubCommand {

    public ShowCreateSubCommand() {
        super("create");
    }

    @Override
    public void onExecute(User sender, String[] args) {
        if (OpenAudioFabric.getService(ShowService.class).createShow(args[1]) == null) {
            sender.sendMessage(ChatColor.RED + "Show can't be created. Is that name already in use?");
        } else {
            sender.sendMessage(ChatColor.GOLD + "Show created!");
        }
    }

}
