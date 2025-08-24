package me.jetby.treexBuyer.tools;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class Logger {

    public void warn(String message) {
        Bukkit.getConsoleSender().sendMessage("§e[TreexBuyer] §e"+ message);
    }
    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage("§a[TreexBuyer] §f"+ message);
    }
    public void success(String message) {
        Bukkit.getConsoleSender().sendMessage("§a[TreexBuyer] §a"+ message);
    }
    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§c[TreexBuyer] §c"+ message);
    }
    public void msg(String message) {
        Bukkit.getConsoleSender().sendMessage("§6[TreexBuyer] §f"+ message);
    }
}