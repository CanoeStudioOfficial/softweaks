package com.canoestudio.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.CommandEvent;
import com.canoestudio.config.ConfigHandler;
import com.canoestudio.config.RankHandler;

public class CommandHandler {
    public static void handleCommand(CommandEvent event) {
        ICommandSender sender = event.getSender();
        String commandName = event.getCommand().getName();
        String playerName = sender.getName();

        RankHandler.Rank rank = RankHandler.getPlayerRank(playerName);

        if (rank == RankHandler.Rank.ADMIN) {
            return;
        } else if (rank == RankHandler.Rank.USER && ConfigHandler.isCommandWhitelisted(commandName)) {

            return;
        } else {
            event.setCanceled(true);
            sender.sendMessage(new TextComponentTranslation("message.command_not_allowed"));
        }
    }
}