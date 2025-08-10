package com.yaesaraki.studyLife.util;

import com.yaesaraki.studyLife.StudyLifePlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Log {

    private static final JavaPlugin plugin = StudyLifePlugin.getInstance();
    private static final String PREFIX = "[StudyLife] ";

    private static boolean isDebug() {
        return plugin.getConfig().getBoolean("debug", false);
    }

    // 普通信息
    public static void info(String msg) {
        plugin.getLogger().info(PREFIX + msg);
    }

    // 警告信息
    public static void warn(String msg) {
        plugin.getLogger().warning(PREFIX + msg);
    }

    // 错误信息（非崩溃）
    public static void error(String msg) {
        plugin.getLogger().severe(PREFIX + msg);
    }

    // 错误信息（附带异常堆栈）
    public static void error(String msg, Throwable t) {
        plugin.getLogger().severe(PREFIX + msg);
        t.printStackTrace();
    }

    // 仅当 debug 模式启用时输出
    public static void debug(String msg) {
        if (isDebug()) {
            plugin.getLogger().info(PREFIX + "[DEBUG] " + msg);
        }
    }

    // 可选调试堆栈
    public static void debug(String msg, Throwable t) {
        if (isDebug()) {
            plugin.getLogger().info(PREFIX + "[DEBUG] " + msg);
            t.printStackTrace();
        }
    }
}