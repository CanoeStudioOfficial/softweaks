package com.canoestudio.config;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class BlockInteractHandler {

    private static String interactMessage;
    private static String insufficientExperienceMessage;
    private static int requiredExperienceLevels;

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "interact_block_config.txt");
        Configuration config = new Configuration(configFile);
        config.load();

        Property interactMessageProp = config.get(Configuration.CATEGORY_GENERAL, "interact_message", "互动成功，已消耗 {0} 级经验。");
        Property insufficientExperienceMessageProp = config.get(Configuration.CATEGORY_GENERAL, "insufficient_experience_message", "互动失败，您的等级不足 {0} 级。");
        Property requiredExperienceLevelsProp = config.get(Configuration.CATEGORY_GENERAL, "required_experience_levels", 5);

        interactMessage = interactMessageProp.getString();
        insufficientExperienceMessage = insufficientExperienceMessageProp.getString();
        requiredExperienceLevels = requiredExperienceLevelsProp.getInt();

        if (config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.world.isRemote) {
            int playerExperienceLevel = player.experienceLevel;
            if (playerExperienceLevel >= requiredExperienceLevels) {
                player.addExperienceLevel(-requiredExperienceLevels);
                player.sendMessage(new TextComponentTranslation(interactMessage, requiredExperienceLevels));
            } else {
                player.sendMessage(new TextComponentTranslation(insufficientExperienceMessage, requiredExperienceLevels));
            }
        }
    }
}