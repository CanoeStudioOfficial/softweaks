package com.canoestudio.config;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class ConfigHandler {
    private static Set<String> whitelist;
    private static Set<String> superUsers;
    private static File whitelistFile;
    private static File superUsersFile;

    public static void init(File configDir) {
        whitelistFile = new File(configDir, "whitelist.txt");
        superUsersFile = new File(configDir, "superusers.txt");
        whitelist = new HashSet<>();
        superUsers = new HashSet<>();

        // 加载白名单
        if (whitelistFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(whitelistFile))) {
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
            saveWhitelistConfig();
        }

        // 加载超级用户列表
        if (superUsersFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(superUsersFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    superUsers.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 如果文件不存在，可以选择初始化为空文件
            saveSuperUsersConfig();
        }
    }

    public static boolean isCommandWhitelisted(String commandName) {
        return whitelist.contains(commandName);
    }

    public static boolean isSuperUser(String playerName) {
        return superUsers.contains(playerName);
    }

    public static void addCommandToWhitelist(String commandName) {
        if (!whitelist.contains(commandName)) {
            whitelist.add(commandName);
            saveWhitelistConfig();
        }
    }

    public static void addSuperUser(String playerName) {
        if (!superUsers.contains(playerName)) {
            superUsers.add(playerName);
            saveSuperUsersConfig();
        }
    }

    private static void saveWhitelistConfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(whitelistFile))) {
            for (String command : whitelist) {
                writer.write(command);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveSuperUsersConfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(superUsersFile))) {
            for (String user : superUsers) {
                writer.write(user);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}