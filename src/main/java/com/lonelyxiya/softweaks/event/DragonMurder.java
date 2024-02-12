package com.lonelyxiya.softweaks.event;


import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
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

        if (!world.isRemote && world.provider instanceof WorldProviderEnd && entity instanceof EntityDragon) {
            entity.setDead();
        }
    }
}
