package com.yaesaraki.studyLife.data;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private static Connection connection;

    // ✨ 缓存每个玩家的总专注时间（单位：秒）
    private static final ConcurrentHashMap<UUID, Long> focusTimeCache = new ConcurrentHashMap<>();

    // ✨ 标记哪些缓存数据已修改，需同步到数据库
    private static final ConcurrentHashMap<UUID, Boolean> dirtyFlags = new ConcurrentHashMap<>();

    public static void init(String dbPath) {
        try {
            File file = new File(dbPath);
            file.getParentFile().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            Bukkit.getLogger().info("[StudyLife] 成功连接 SQLite 数据库: " + dbPath);

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS study_time (
                        uuid TEXT PRIMARY KEY,
                        seconds INTEGER DEFAULT 0
                    )
                """);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[StudyLife] 无法连接 SQLite 数据库: " + e.getMessage());
            connection = null;
        }
    }

    // ✅ 增加学习时间（加到缓存中，不立刻写入）
    public static void addStudyTime(UUID uuid, long secondsToAdd) {
        focusTimeCache.merge(uuid, secondsToAdd, Long::sum);
        dirtyFlags.put(uuid, true); // 标记为脏数据
    }

    // ✅ 获取缓存中的学习时间（若没有则从数据库加载）
    public static long getStudyTime(UUID uuid) {
        return focusTimeCache.computeIfAbsent(uuid, PlayerDataManager::loadFromDatabase);
    }

    // ✅ 强制将某个玩家缓存写入数据库
    public static void flush(UUID uuid) {
        if (!dirtyFlags.getOrDefault(uuid, false)) return; // 无需同步

        long seconds = focusTimeCache.getOrDefault(uuid, 0L);
        try (PreparedStatement stmt = connection.prepareStatement("""
                INSERT INTO study_time (uuid, seconds)
                VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET seconds = excluded.seconds
        """)) {
            stmt.setString(1, uuid.toString());
            stmt.setLong(2, seconds);
            stmt.executeUpdate();
            dirtyFlags.put(uuid, false); // 重置脏标记
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ 写入所有缓存（用于插件关闭或世界保存等）
    public static void flushAll() {
        for (UUID uuid : focusTimeCache.keySet()) {
            flush(uuid);
        }
    }

    // ✅ 从数据库中加载（用于初始化缓存）
    private static long loadFromDatabase(UUID uuid) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT seconds FROM study_time WHERE uuid = ?"
        )) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("seconds");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    // ✅ 关闭数据库（应在插件停用时调用 flushAll() 后再关闭）
    public static void close() {
        flushAll(); // 写入所有缓存再关闭
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}