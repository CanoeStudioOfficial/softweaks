package com.lonelyxiya.softweaks.event;

import com.google.common.collect.Sets;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Set;

public class TorchModule {
    public static Set<String> torchItems = Sets.newHashSet();
    public static Set<String> torchTools = Sets.newHashSet();
    public static Set<String> offhandTorchTools = Sets.newHashSet();
    private static Configuration config;

    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        reloadConfig();
    }

    private static void reloadConfig() {
        torchItems = Sets.newHashSet(config.getStringList("Torch Items", "general", new String[] {
                "minecraft:torch",
                "tconstruct:stone_torch"
        }, "Items that count as torches for the offhand-torch tweak options."));
        torchTools = Sets.newHashSet(config.getStringList("Torch Tools", "general", new String[] {
                "minecraft:wooden_pickaxe",
                "minecraft:stone_pickaxe",
                "minecraft:iron_pickaxe",
                "minecraft:golden_pickaxe",
                "minecraft:diamond_pickaxe",
                "tconstruct:pickaxe",
                "tconstruct:hammer"
        }, "Items that will place torches from your hotbar on right-click if enabled."));
        offhandTorchTools = Sets.newHashSet(config.getStringList("Offhand Torch Tools", "general", new String[] {
                "tconstruct:shovel",
                "tconstruct:excavator"
        }, "Items that will not prevent offhand-torch placement while in offhand, but do not place torches by themselves"));
    }
}