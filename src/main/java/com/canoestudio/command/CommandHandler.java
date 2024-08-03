package com.canoestudio.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.CommandEvent;
import com.canoestudio.config.ConfigHandler;
import com.canoestudio.config.RankHandler;

import net.minecraft.client.resources.I18n;

public class CommandHandler {
    public static void handleCommand(CommandEvent event) {
        ICommandSender sender = event.getSender();
        String commandName = event.getCommand().getName();
        String playerName = sender.getName();

        // 检查是否为超级用户
        if (ConfigHandler.isSuperUser(playerName)) {
            // 如果是超级用户，允许使用所有指令
            return;
        }

        RankHandler.Rank rank = RankHandler.getPlayerRank(playerName);

        if (rank == RankHandler.Rank.ADMIN) {
            // 如果是管理员，允许使用所有指令
            return;
        } else if (rank == RankHandler.Rank.USER && ConfigHandler.isCommandWhitelisted(commandName)) {
            // 如果是普通用户且指令在白名单中，允许使用
            return;
        } else {
            // 否则禁止使用
            event.setCanceled(true);
            sender.sendMessage(new TextComponentString(I18n.format("message.command_not_allowed")));
        }
    }
}
