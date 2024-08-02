package com.canoestudio.config;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DimensionEnterHandler {

    private static String teleportMessage = "You did not hold the required item and have been teleported to your spawn point.";
    private static Map<Integer, ItemRequirement> dimensionItemRequirements = new HashMap<>();

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "dimension_enter_config.txt");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Configuration config = new Configuration(configFile);
                config.load();

                // 添加默认配置
                config.get(Configuration.CATEGORY_GENERAL, "-1:minecraft:diamond_sword:0", "required");
                config.get(Configuration.CATEGORY_GENERAL, "0:minecraft:golden_apple:1", "required");

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
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        EntityPlayer player = event.player;
        int dimension = event.toDim;

        if (dimensionItemRequirements.containsKey(dimension)) {
            ItemRequirement requirement = dimensionItemRequirements.get(dimension);
            ItemStack heldItem = player.getHeldItemMainhand();

            boolean hasRequiredItem = heldItem.getItem() == ForgeRegistries.ITEMS.getValue(requirement.item) && heldItem.getMetadata() == requirement.meta;

            if (hasRequiredItem) {
                heldItem.shrink(1); // 消耗一个物品
            } else {
                if (dimension == -1) { // 特殊处理进入地狱
                    player.setPositionAndUpdate(player.getBedLocation().getX(), player.getBedLocation().getY(), player.getBedLocation().getZ());
                    player.sendMessage(new TextComponentTranslation(teleportMessage));
                }
            }
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