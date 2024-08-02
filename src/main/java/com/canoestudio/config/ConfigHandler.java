package com.canoestudio.config;

import net.minecraft.util.text.TextComponentTranslation;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class ConfigHandler {
    private static Set<String> whitelist;
    private static File configFile;

    public static void init(File configDir) {
        configFile = new File(configDir, "whitelist.txt");
        whitelist = new HashSet<>();
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    whitelist.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 添加基础常用的指令
            whitelist.add("activateadmin");
            whitelist.add("help");
            whitelist.add("list");
            whitelist.add("say");
            whitelist.add("tp");
            whitelist.add("give");
            saveConfig();
            System.out.println(new TextComponentTranslation("message.whitelist_initialized").getFormattedText());
        }
    }

    public static boolean isCommandWhitelisted(String commandName) {
        return whitelist.contains(commandName);
    }

    public static void addCommandToWhitelist(String commandName) {
        if (!whitelist.contains(commandName)) {
            whitelist.add(commandName);
            saveConfig();
        }
    }

    private static void saveConfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            for (String command : whitelist) {
                writer.write(command);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
