package com.canoestudio.config;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.resources.I18n;

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
                        "minecraft:crafting_table:0=5",
                        "minecraft:furnace:0=5",
                        "minecraft:chest:0=5"
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
            if (parts.length == 3) {
                String blockId = parts[0] + ":" + parts[1];
                String[] metaAndLevel = parts[2].split("=");
                int meta = Integer.parseInt(metaAndLevel[0]);
                int level = Integer.parseInt(metaAndLevel[1]);
                blockInteractions.put(new BlockKey(blockId, meta), level);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) {
            return; // 仅在服务器端处理
        }

        // 检查右键是否针对方块
        EntityPlayer player = event.getEntityPlayer();
        if (player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) {
            return; // 如果没有物品在手，不处理
        }

        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
        int meta = block.getMetaFromState(event.getWorld().getBlockState(event.getPos()));
        BlockKey key = new BlockKey(block.getRegistryName().toString(), meta);

        if (blockInteractions.containsKey(key)) {
            int requiredExperienceLevels = blockInteractions.get(key);
            if (player.experienceLevel >= requiredExperienceLevels) {
                player.addExperienceLevel(-requiredExperienceLevels);
                player.sendMessage(new TextComponentString(I18n.format("message.block_interact_success", block.getRegistryName().toString(), requiredExperienceLevels)));
            } else {
                player.sendMessage(new TextComponentString(I18n.format("message.not_enough_experience", requiredExperienceLevels)));
                event.setCanceled(true); // 取消交互，阻止GUI打开
            }
        }
    }

    private static class BlockKey {
        private final String blockId;
        private final int meta;

        BlockKey(String blockId, int meta) {
            this.blockId = blockId;
            this.meta = meta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BlockKey blockKey = (BlockKey) o;

            if (meta != blockKey.meta) return false;
            return blockId.equals(blockKey.blockId);
        }

        @Override
        public int hashCode() {
            int result = blockId.hashCode();
            result = 31 * result + meta;
            return result;
        }
    }
}