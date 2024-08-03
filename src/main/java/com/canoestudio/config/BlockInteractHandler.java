package com.canoestudio.config;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockInteractHandler {

    private static Map<BlockKey, Integer> blockInteractions = new HashMap<>();

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "block_interact_config.txt");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Configuration config = new Configuration(configFile);
                config.load();

                // 添加默认配置
                config.get(Configuration.CATEGORY_GENERAL, "interactBlocks", new String[]{
                        "minecraft:diamond_block:0:right=5",
                        "minecraft:stone:1:left=3"
                });

                if (config.hasChanged()) {
                    config.save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Configuration config = new Configuration(configFile);
        config.load();

        String[] interactBlocks = config.get(Configuration.CATEGORY_GENERAL, "interactBlocks", new String[]{}).getStringList();
        for (String entry : interactBlocks) {
            String[] parts = entry.split(":");
            if (parts.length == 4) {
                String blockId = parts[0] + ":" + parts[1];
                int meta = Integer.parseInt(parts[2]);
                String[] handAndLevel = parts[3].split("=");
                String hand = handAndLevel[0];
                int level = Integer.parseInt(handAndLevel[1]);
                blockInteractions.put(new BlockKey(blockId, meta, hand), level);
            }
        }
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event instanceof PlayerInteractEvent.RightClickBlock || event instanceof PlayerInteractEvent.LeftClickBlock) {
            EntityPlayer player = event.getEntityPlayer();
            if (!player.world.isRemote) {
                Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
                int meta = block.getMetaFromState(event.getWorld().getBlockState(event.getPos()));
                String hand = event.getHand() == EnumHand.MAIN_HAND ? "right" : "left";
                BlockKey key = new BlockKey(block.getRegistryName().toString(), meta, hand);

                if (blockInteractions.containsKey(key)) {
                    int requiredExperienceLevels = blockInteractions.get(key);
                    if (player.experienceLevel >= requiredExperienceLevels) {
                        player.addExperienceLevel(-requiredExperienceLevels);
                        player.sendMessage(new TextComponentString(I18n.format("message.block_interact_success", block.getRegistryName().toString(), hand, requiredExperienceLevels)));
                    } else {
                        player.sendMessage(new TextComponentString(I18n.format("message.not_enough_experience", requiredExperienceLevels)));
                    }
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