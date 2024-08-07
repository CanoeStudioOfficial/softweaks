package com.canoestudio.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.CommandEvent;
import com.canoestudio.config.ConfigHandler;
import com.canoestudio.config.RankHandler;

import net.minecraft.client.resources.I18n;

public class CommandHandler {

    private static boolean isIntegratedServer;

    public CommandHandler(boolean isIntegratedServer) {
        this.isIntegratedServer = isIntegratedServer;
    }

    public static void handleCommand(CommandEvent event) {
        ICommandSender sender = event.getSender();
        String commandName = event.getCommand().getName();
        String playerName = sender.getName();

        // 添加调试信息
        System.out.println("Processing command: " + commandName + " from player: " + playerName);
        System.out.println("Server type: " + (isIntegratedServer ? "Integrated Server" : "Dedicated Server"));

        // 如果是集成服务器，允许所有指令
        if (isIntegratedServer) {
            System.out.println("Integrated server detected, allowing all commands.");
            return;
        }

        // 检查是否为超级用户
        if (ConfigHandler.isSuperUser(playerName)) {
            System.out.println("Player is a super user, allowing command.");
            // 如果是超级用户，允许使用所有指令
            return;
        }

        RankHandler.Rank rank = RankHandler.getPlayerRank(playerName);

        if (rank == RankHandler.Rank.ADMIN) {
            System.out.println("Player is an admin, allowing command.");
            // 如果是管理员，允许使用所有指令
            return;
        } else if (rank == RankHandler.Rank.USER && ConfigHandler.isCommandWhitelisted(commandName)) {
            System.out.println("Command is whitelisted, allowing command.");
            // 如果是普通用户且指令在白名单中，允许使用
            return;
        } else {
            System.out.println("Command not allowed for player.");
            // 否则禁止使用
            event.setCanceled(true);
            sender.sendMessage(new TextComponentString(I18n.format("message.command_not_allowed")));
        }
    }
}
