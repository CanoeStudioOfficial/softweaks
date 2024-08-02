package com.canoestudio.config;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class BlockInteractHandler {

    private static String interactMessage = "Interaction successful, {0} levels of experience have been consumed.";
    private static String insufficientExperienceMessage = "Interaction failed, you do not have enough experience levels.";
    private static Map<String, Integer> blockExperienceLevels = new HashMap<>();

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "interact_block_config.txt");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Configuration config = new Configuration(configFile);
                config.load();

                // 添加默认配置
                config.get(Configuration.CATEGORY_GENERAL, "minecraft:diamond_block:right", 5).getInt();

                if (config.hasChanged()) {
                    config.save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Configuration config = new Configuration(configFile);
        config.load();

        for (String key : config.getCategory(Configuration.CATEGORY_GENERAL).keySet()) {
            blockExperienceLevels.put(key, config.get(Configuration.CATEGORY_GENERAL, key, 0).getInt());
        }
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.world.isRemote) {
            Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
            String hand = event.getHand() == EnumHand.MAIN_HAND ? "right" : "left";
            String key = block.getRegistryName().toString() + ":" + hand;

            if (blockExperienceLevels.containsKey(key)) {
                int requiredExperienceLevels = blockExperienceLevels.get(key);
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
}