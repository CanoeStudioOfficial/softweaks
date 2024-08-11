package com.canoestudio.config;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n; 
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BlockinteractionHandler {

    private static Map<BlockData, Integer> blockExperienceMap = new HashMap<>();
    private static boolean defaultBlockInteraction;

    public BlockinteractionHandler() {
        loadConfig(); // 在构造函数中加载配置
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) return;

        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
        int meta = event.getWorld().getBlockState(event.getPos()).getBlock().getMetaFromState(event.getWorld().getBlockState(event.getPos()));
        BlockData blockData = new BlockData(block, meta);

        if (blockExperienceMap.containsKey(blockData)) {
            int requiredExperienceLevel = blockExperienceMap.get(blockData);
            if (event.getEntityPlayer().experienceLevel < requiredExperienceLevel) {
                event.setCanceled(true);
                event.getEntityPlayer().sendMessage(new TextComponentString(I18n.format("blockinteractionmod.lowExperienceLevelMessage", requiredExperienceLevel)));
                return;
            }
        }

        if (isBlocked(block, meta)) {
            event.setCanceled(true);
            event.getEntityPlayer().sendMessage(new TextComponentString(I18n.format("blockinteractionmod.blockedBlockMessage")));
        }
    }

    private static boolean isBlocked(Block block, int meta) {
        BlockData blockData = new BlockData(block, meta);
        return blockExperienceMap.containsKey(blockData) && !defaultBlockInteraction;
    }

    private static void loadConfig() {
        Configuration config = new Configuration(new File("config/blockinteractionmod.cfg"));
        config.load();

        backupConfigFile(config);

        defaultBlockInteraction = config.getBoolean("defaultBlockInteraction", "general", false,
                "Whether block interaction is allowed by default");

        blockExperienceMap.clear();
        Map<BlockData, Integer> defaultBlockExperienceMap = new HashMap<>();
        defaultBlockExperienceMap.put(new BlockData(Blocks.CRAFTING_TABLE, 0), 5); // Example: crafting table requires level 5

        blockExperienceMap.putAll(getBlockExperienceFromConfig(config, "blockExperienceLevels", defaultBlockExperienceMap));

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static void backupConfigFile(Configuration config) {
        File configFile = config.getConfigFile();

        if (config.hasChanged()) {
            // 这里你可以实现备份逻辑
        }
    }

    private static Map<BlockData, Integer> getBlockExperienceFromConfig(Configuration config, String category, Map<BlockData, Integer> defaultValues) {
        Map<BlockData, Integer> blockExperience = new HashMap<>();
        String[] blockEntries = config.getStringList("blockExperienceLevels", category, getBlockExperienceAsStringArray(defaultValues), "List of blocks with required experience levels");
        for (String blockEntry : blockEntries) {
            String[] parts = blockEntry.split(":");
            if (parts.length == 4) { // 修复数组索引问题，确保parts长度正确
                Block block = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation(parts[0], parts[1]));
                if (block != null) {
                    try {
                        int meta = Integer.parseInt(parts[2]);
                        int requiredLevel = Integer.parseInt(parts[3]);
                        blockExperience.put(new BlockData(block, meta), requiredLevel);
                    } catch (NumberFormatException e) {
                        // 处理无效的元数据或经验等级
                    }
                }
            }
        }
        return blockExperience;
    }

    private static String[] getBlockExperienceAsStringArray(Map<BlockData, Integer> blockExperienceMap) {
        String[] dataArray = new String[blockExperienceMap.size()];
        int index = 0;
        for (Map.Entry<BlockData, Integer> entry : blockExperienceMap.entrySet()) {
            dataArray[index++] = entry.getKey().toString() + ":" + entry.getValue();
        }
        return dataArray;
    }

    private static class BlockData {
        public Block block;
        public int meta;

        public BlockData(Block block, int meta) {
            this.block = block;
            this.meta = meta;
        }

        @Override
        public String toString() {
            return block.getRegistryName().toString() + ":" + meta;
        }

        @Override
        public int hashCode() {
            return block.hashCode() * 31 + meta;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            BlockData other = (BlockData) obj;
            return block == other.block && meta == other.meta;
        }
    }
}
