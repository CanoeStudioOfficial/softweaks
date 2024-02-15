package com.lonelyxiya.softweaks;

import com.lonelyxiya.softweaks.event.DragonMurder;
import com.lonelyxiya.softweaks.event.ExperienceModule;
import com.lonelyxiya.softweaks.event.TorchModule;
import com.lonelyxiya.softweaks.event.LadderModule;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(
        modid = Softweaks.MOD_ID,
        name = "Softweaks",
        acceptedMinecraftVersions = "[1.12]",
        clientSideOnly = true,
        guiFactory = "net.blay09.mods.clienttweaks.GuiFactory"
)
public class Softweaks {
    public static final String MOD_ID = "softweaks";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        new TorchModule().preInit(event);

        new LadderModule().preInit(event);

        new DragonMurder().preInit(event);

        new ExperienceModule().preInit(event);
    }
}

