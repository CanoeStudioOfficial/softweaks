package com.canoestudio.config;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DimensionEnterHandler {

    private static String preventBreakMessage = "You did not hold the required item and cannot break blocks in the Nether.";
    private static Map<Integer, ItemRequirement> dimensionItemRequirements = new HashMap<>();
    private static Set<String> playersWithoutItem = new HashSet<>();

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "dimension_enter_config.txt");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Configuration config = new Configuration(configFile);
                config.load();

                // 添加默认配置
                config.get(Configuration.CATEGORY_GENERAL, "-1:minecraft:diamond_sword:0", "required");

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
                int dimension = Integer.parseInt(parts[0]);
                ResourceLocation itemResource = new ResourceLocation(parts[1], parts[2]);
                int meta = Integer.parseInt(parts[3]);
                dimensionItemRequirements.put(dimension, new ItemRequirement(itemResource, meta));
            }
        }
    }

    @SubscribeEvent
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            int dimension = event.getDimension();

            if (dimensionItemRequirements.containsKey(dimension)) {
                ItemRequirement requirement = dimensionItemRequirements.get(dimension);
                ItemStack heldItem = player.getHeldItemMainhand();

                boolean hasRequiredItem = heldItem.getItem() == ForgeRegistries.ITEMS.getValue(requirement.item) && heldItem.getMetadata() == requirement.meta;

                if (dimension == -1 && !hasRequiredItem) {
                    playersWithoutItem.add(player.getUniqueID().toString());
                    player.sendMessage(new TextComponentTranslation(preventBreakMessage));
                } else {
                    playersWithoutItem.remove(player.getUniqueID().toString());
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player.world.provider.getDimension() == -1 && playersWithoutItem.contains(player.getUniqueID().toString())) {
            event.setCanceled(true);
            player.sendMessage(new TextComponentTranslation(preventBreakMessage));
        }
    }

    private static class ItemRequirement {
        ResourceLocation item;
        int meta;

        ItemRequirement(ResourceLocation item, int meta) {
            this.item = item;
            this.meta = meta;
        }
    }
}