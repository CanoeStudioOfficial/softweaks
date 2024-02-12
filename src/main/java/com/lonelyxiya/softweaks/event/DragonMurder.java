package com.lonelyxiya.softweaks.event;


import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.biome.BiomeEndDecorator;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.feature.WorldGenSpikes;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class DragonMurder {

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void join(EntityJoinWorldEvent e) {
        Entity entity = e.getEntity();
        World world = entity.getEntityWorld();

        if (!world.isRemote && world.provider instanceof WorldProviderEnd) {
            WorldProviderEnd endProvider = (WorldProviderEnd) world.provider;
            DragonFightManager dragonFightManager = endProvider.getDragonFightManager();

            if (entity instanceof EntityDragon && dragonFightManager != null && !dragonFightManager.hasPreviouslyKilledDragon()) {

                EntityDragon dragon = new EntityDragon(world);
                dragon.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, world.rand.nextFloat() * 360.0F, 0.0F);
                world.spawnEntity(dragon);


                for (WorldGenSpikes.EndSpike spike : BiomeEndDecorator.getSpikesForWorld(world)) {
                    world.getEntitiesWithinAABB(EntityEnderCrystal.class, spike.getTopBoundingBox())
                            .forEach(crystal -> crystal.setDead());
                }
            }
        }
    }
}