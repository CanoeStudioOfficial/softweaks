package com.canoestudio.config;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DimensionEnterHandler {

    private static String missingItemMessage = "You do not have the required item to enter the Nether.";
    private static Map<Integer, DimensionRequirement> dimensionItemRequirements = new HashMap<>();

    public static void initConfig(File configDir) {
        File configFile = new File(configDir, "dimension_enter_config.txt");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                Configuration config = new Configuration(configFile);
                config.load();

                // 添加默认配置
                config.get(Configuration.CATEGORY_GENERAL, "-1:minecraft:diamond_sword:0:right", "required");

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
            if (parts.length == 5) {
                int dimension = Integer.parseInt(parts[0]);
                ResourceLocation itemResource = new ResourceLocation(parts[1], parts[2]);
                int meta = Integer.parseInt(parts[3]);
                String hand = parts[4];
                dimensionItemRequirements.put(dimension, new DimensionRequirement(itemResource, meta, hand));
            }
        }
    }

    @SubscribeEvent
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            int dimension = event.getDimension();

            if (dimensionItemRequirements.containsKey(dimension)) {
                DimensionRequirement requirement = dimensionItemRequirements.get(dimension);

                boolean hasRequiredItem = false;

                // 检查玩家背包是否有指定物品
                for (ItemStack stack : player.inventory.mainInventory) {
                    if (stack.getItem() == ForgeRegistries.ITEMS.getValue(requirement.item) && stack.getMetadata() == requirement.meta) {
                        hasRequiredItem = true;
                        stack.shrink(1); // 消耗一个物品
                        break;
                    }
                }

                if (!hasRequiredItem) { // 特殊处理进入地狱
                    player.sendMessage(new TextComponentTranslation(missingItemMessage));
                    event.setCanceled(true); // 阻止传送到地狱
                }
            }
        }
    }

    private static class DimensionRequirement {
        ResourceLocation item;
        int meta;
        String hand;

        DimensionRequirement(ResourceLocation item, int meta, String hand) {
            this.item = item;
            this.meta = meta;
            this.hand = hand;
        }
    }
}