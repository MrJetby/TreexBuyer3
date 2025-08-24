package me.jetby.treexBuyer.menus.commands.impl;

import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.commands.Action;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TitleAction implements Action {
    @Override
    public void execute(@NotNull Player player, String context, Button button) {
        var args = context.split(";");
        var title = args.length > 0 ? args[0] : "";
        var subTitle = args.length > 1 ? args[1] : "";
        int fadeIn = args.length > 2 ? Integer.valueOf(args[2]) : 10;
        int stayIn = args.length > 3 ? Integer.valueOf(args[3]) : 70;
        int fadeOut = args.length > 4 ? Integer.valueOf(args[4]) : 20;
        player.sendTitle(title, subTitle, fadeIn, stayIn, fadeOut);
    }
}