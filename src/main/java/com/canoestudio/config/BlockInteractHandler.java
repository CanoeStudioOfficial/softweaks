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

    private static String interactMessageKey = "message.interact_success";
    private static String insufficientExperienceMessageKey = "message.not_enough_experience";
    private static Map<BlockKey, Integer> blockExperienceLevels = new HashMap<>();

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "interact_block_config.txt");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Configuration config = new Configuration(configFile);
                config.load();

                // 添加默认配置
                config.get(Configuration.CATEGORY_GENERAL, "minecraft:diamond_block:0:right", 5).getInt();

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
            String[] parts = key.split(":");
            if (parts.length == 4) {
                String blockId = parts[0] + ":" + parts[1];
                int meta = Integer.parseInt(parts[2]);
                String hand = parts[3];
                int level = config.get(Configuration.CATEGORY_GENERAL, key, 0).getInt();
                blockExperienceLevels.put(new BlockKey(blockId, meta, hand), level);
            }
        }
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.world.isRemote) {
            Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
            int meta = block.getMetaFromState(event.getWorld().getBlockState(event.getPos()));
            String hand = event.getHand() == EnumHand.MAIN_HAND ? "right" : "left";
            BlockKey key = new BlockKey(block.getRegistryName().toString(), meta, hand);

            if (blockExperienceLevels.containsKey(key)) {
                int requiredExperienceLevels = blockExperienceLevels.get(key);
                int playerExperienceLevel = player.experienceLevel;
                if (playerExperienceLevel >= requiredExperienceLevels) {
                    player.addExperienceLevel(-requiredExperienceLevels);
                    player.sendMessage(new TextComponentTranslation(interactMessageKey, requiredExperienceLevels));
                } else {
                    player.sendMessage(new TextComponentTranslation(insufficientExperienceMessageKey, requiredExperienceLevels));
                }
            }
        }
    }

    private static class BlockKey {
        private final String blockId;
        private final int meta;
        private final String hand;

        BlockKey(String blockId, int meta, String hand) {
            this.blockId = blockId;
            this.meta = meta;
            this.hand = hand;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BlockKey blockKey = (BlockKey) o;

            if (meta != blockKey.meta) return false;
            if (!blockId.equals(blockKey.blockId)) return false;
            return hand.equals(blockKey.hand);
        }

        @Override
        public int hashCode() {
            int result = blockId.hashCode();
            result = 31 * result + meta;
            result = 31 * result + hand.hashCode();
            return result;
        }
    }
}