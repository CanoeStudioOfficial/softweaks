package com.canoestudio.command;

import com.canoestudio.config.RankHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandActivateAdmin extends CommandBase {
    @Override
    public String getName() {
        return "activateadmin";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/activateadmin <player> <key>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString("Usage: " + getUsage(sender)));
            return;
        }

        String targetPlayerName = args[0];
        String key = args[1];

        EntityPlayer targetPlayer;
        try {
            targetPlayer = getPlayer(server, sender, targetPlayerName);
        } catch (CommandException e) {
            sender.sendMessage(new TextComponentString("Player not found: " + targetPlayerName));
            return;
        }

        if (targetPlayer == null) {
            sender.sendMessage(new TextComponentString("Player not found: " + targetPlayerName));
            return;
        }

        if (RankHandler.activateAdmin(targetPlayerName, key)) {
            sender.sendMessage(new TextComponentString("Admin privileges activated for " + targetPlayerName + "."));
            targetPlayer.sendMessage(new TextComponentString("You have been granted admin privileges."));
        } else {
            sender.sendMessage(new TextComponentString("Invalid key."));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true; // 任何人都可以尝试激活管理员权限
    }
}
