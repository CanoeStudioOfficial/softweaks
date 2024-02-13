package com.lonelyxiya.softweaks.event;


import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber()
public class DragonMurder {


    public static final Logger LOGGER = LogManager.getLogger();

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void dragonmurder(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        World world = event.getWorld();

        if (!world.isRemote && world.provider.getDimension() == 1) {

            if (entity instanceof EntityDragon) {
                List<EntityEnderCrystal> list = world.getEntitiesWithinAABB(EntityEnderCrystal.class, entity.getEntityBoundingBox().grow(320.0D));
                for (EntityEnderCrystal enderCrystal : list) {
                    enderCrystal.setDead();
                }

                ((EntityDragon) entity).setHealth(0.0F);
                LOGGER.debug("Good night Ender Dragon.");
            }
        }
    }
}