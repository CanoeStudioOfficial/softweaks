package com.lonelyxiya.softweaks.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExperienceModule {
    private boolean keepXP;
    private double xpLoss;
    private double xpRecover;

    public ExperienceModule() {
        super();
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        loadConfig(event);
    }

    private void loadConfig(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        keepXP = config.getBoolean("Keep XP", Configuration.CATEGORY_GENERAL, false,
                "Set to true to keep XP on death");
        xpLoss = config.getFloat("XP Loss", Configuration.CATEGORY_GENERAL, 0.5F, 0.0F, 1.0F,
                "Percentage of experience lost on death");
        xpRecover = config.getFloat("XP Recover", Configuration.CATEGORY_GENERAL, 0.2F, 0.0F, 1.0F,
                "Percentage of lost experience that can be recovered");

        config.save();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (!keepXP) {
                int totalXP = player.experienceTotal;
                int xpToKeep = (int) (totalXP * xpRecover);
                int xpToDrop = (int) (totalXP * xpLoss);
                player.experienceTotal = xpToKeep;
                player.experience = 0.0F;
                player.experienceLevel = 0;
                player.experience += (float) xpToKeep / (float) player.xpBarCap();
                player.experienceLevel = Math.min(player.experienceLevel, player.xpBarCap());
                player.experienceTotal = xpToKeep;
                player.addExperience(xpToDrop);
            }
        }
    }
}