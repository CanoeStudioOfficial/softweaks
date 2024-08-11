package com.canoestudio.config;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.client.resources.I18n;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BlockInteractHandler {

    private static Set<BlockData> interactBlocks = new HashSet<>();
    private boolean isIntegratedServer;
    private boolean isDedicatedServer;

    public BlockInteractHandler() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            isIntegratedServer = server.isSinglePlayer();
            isDedicatedServer = server.isDedicatedServer();
        } else {
            isIntegratedServer = false;
            isDedicatedServer = false;
        }
    }

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "block_interact_config.txt");
        Configuration config = new Configuration(configFile);

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config.load();

        // 从配置文件加载需要处理的方块列表
        String[] defaultInteractBlocks = {
                "minecraft:crafting_table:0=5",
                "minecraft:furnace:0=5",
                "minecraft:chest:0=5"
        };
        interactBlocks.addAll(getBlocksFromConfig(config, "interactBlocks", defaultInteractBlocks));

        if (config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) return;

        // 集成服务器和独立服务器的判断
        if (isIntegratedServer) {
            // 集成服务器的逻辑
        } else if (isDedicatedServer) {
            // 独立服务器的逻辑
        }

        EntityPlayer player = event.getEntityPlayer();
        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
        int meta = event.getWorld().getBlockState(event.getPos()).getBlock().getMetaFromState(event.getWorld().getBlockState(event.getPos()));

        if (isInteractableBlock(block, meta)) {
            int requiredExperienceLevels = getRequiredExperienceLevels(block, meta);
            if (player.experienceLevel >= requiredExperienceLevels) {
                player.addExperienceLevel(-requiredExperienceLevels);
                player.sendMessage(new TextComponentString(I18n.format("message.block_interact_success", block.getRegistryName().toString(), requiredExperienceLevels)));
            } else {
                player.sendMessage(new TextComponentString(I18n.format("message.not_enough_experience", requiredExperienceLevels)));
                event.setCanceled(true);
            }
        }
    }

    private static boolean isInteractableBlock(Block block, int meta) {
        for (BlockData interactBlock : interactBlocks) {
            if (interactBlock.block == block && interactBlock.meta == meta) {
                return true;
            }
        }
        return false;
    }

    private static int getRequiredExperienceLevels(Block block, int meta) {
        // 根据方块和元数据获取所需经验等级，可以自定义
        return 5; // 默认返回5级经验作为示例
    }

    private static Set<BlockData> getBlocksFromConfig(Configuration config, String category, String[] defaultValues) {
        Set<BlockData> blocks = new HashSet<>();
        String[] blockNames = config.getStringList(category, "general", defaultValues, "List of interactable blocks");
        for (String blockName : blockNames) {
            String[] parts = blockName.split(":");
            if (parts.length == 3) {
                Block block = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation(parts[0], parts[1]));
                if (block != null) {
                    int meta = Integer.parseInt(parts[2].split("=")[0]);
                    blocks.add(new BlockData(block, meta));
                }
            }
        }
        return blocks;
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
    }
}