package com.canoestudio.config;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;

public class BlockInteractHandler {

    private static String interactMessage;
    private static String insufficientExperienceMessage;
    private static int requiredExperienceLevels;

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "interact_block_config.txt");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Configuration config = new Configuration(configFile);
                config.load();

                Property interactMessageProp = config.get(Configuration.CATEGORY_GENERAL, "interact_message", "Interaction successful, {0} levels of experience have been consumed.");
                Property insufficientExperienceMessageProp = config.get(Configuration.CATEGORY_GENERAL, "insufficient_experience_message", "Interaction failed, you do not have enough experience levels.");
                Property requiredExperienceLevelsProp = config.get(Configuration.CATEGORY_GENERAL, "required_experience_levels", 5);

                interactMessage = interactMessageProp.getString();
                insufficientExperienceMessage = insufficientExperienceMessageProp.getString();
                requiredExperienceLevels = requiredExperienceLevelsProp.getInt();

                if (config.hasChanged()) {
                    config.save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Configuration config = new Configuration(configFile);
            config.load();

            interactMessage = config.get(Configuration.CATEGORY_GENERAL, "interact_message", "Interaction successful, {0} levels of experience have been consumed.").getString();
            insufficientExperienceMessage = config.get(Configuration.CATEGORY_GENERAL, "insufficient_experience_message", "Interaction failed, you do not have enough experience levels.").getString();
            requiredExperienceLevels = config.get(Configuration.CATEGORY_GENERAL, "required_experience_levels", 5).getInt();
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