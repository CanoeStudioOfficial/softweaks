package com.canoestudio.config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RankHandler {
    public enum Rank {
        USER,
        ADMIN
    }

    private static Map<String, Rank> playerRanks;
    private static String adminKey;
    private static File rankFile;
    private static File keyFile;

    public static void init(File configDir) {
        rankFile = new File(configDir, "ranks.txt");
        keyFile = new File(configDir, "adminKey.txt");
        playerRanks = new HashMap<>();

        if (rankFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(rankFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String playerName = parts[0].trim();
                        Rank rank = Rank.valueOf(parts[1].trim());
                        playerRanks.put(playerName, rank);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (keyFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
                adminKey = reader.readLine().trim();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            adminKey = "defaultAdminKey"; // 默认管理员密钥，可以在第一次运行时手动更改
            saveKeyConfig();
        }
    }

    public static Rank getPlayerRank(String playerName) {
        if (!playerRanks.containsKey(playerName)) {
            setPlayerRank(playerName, Rank.USER); // 默认设置为USER
        }
        return playerRanks.getOrDefault(playerName, Rank.USER);
    }

    public static void setPlayerRank(String playerName, Rank rank) {
        playerRanks.put(playerName, rank);
        saveConfig(rankFile);
    }

    public static boolean activateAdmin(String playerName, String key) {
        if (adminKey.equals(key)) {
            setPlayerRank(playerName, Rank.ADMIN);
            return true;
        }
        return false;
    }

    private static void saveConfig(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Rank> entry : playerRanks.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue().name());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveKeyConfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
            writer.write(adminKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
