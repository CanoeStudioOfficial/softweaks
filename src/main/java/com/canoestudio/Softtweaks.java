package com.canoestudio;

import com.canoestudio.command.CommandActivateAdmin;
import com.canoestudio.config.ConfigHandler;
import com.canoestudio.config.RankHandler;
import com.canoestudio.softtweaks.Tags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class Softtweaks {

    private static Set<String> unsupportedMods = new HashSet<>();

    @Mod.Instance
    public static Softtweaks instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // 初始化配置文件
        File configDir = new File(event.getModConfigurationDirectory(), "softtweaks");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        // 加载配置文件
        ConfigHandler.init(configDir);
        RankHandler.init(configDir); // 初始化 RankHandler

        // 加载不支持的MOD列表
        File unsupportedModsFile = new File(configDir, "unsupported_mods.txt");
        if (unsupportedModsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(unsupportedModsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    unsupportedMods.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 注册事件
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // 注册命令
        event.registerServerCommand(new CommandActivateAdmin());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        String playerName = event.player.getName();
        RankHandler.getPlayerRank(playerName); // 确保玩家在登录时被赋予默认Rank

        // 检测已安装的MOD
        boolean hasUnsupportedMod = false;
        List<ModContainer> modList = Loader.instance().getModList();
        for (ModContainer mod : modList) {
            if (unsupportedMods.contains(mod.getModId())) {
                hasUnsupportedMod = true;
                break;
            }
        }

        if (hasUnsupportedMod) {
            // 给予虚弱10级的BUFF，持续30秒（600 ticks）
            EntityPlayer player = event.player;
            PotionEffect weakness = new PotionEffect(MobEffects.WEAKNESS, 600, 9, false, false);
            player.addPotionEffect(weakness);
            player.sendMessage(new TextComponentString("You have been given Weakness because you have an unsupported mod installed."));
        }
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        // 处理指令事件
        com.canoestudio.command.CommandHandler.handleCommand(event);
    }
}