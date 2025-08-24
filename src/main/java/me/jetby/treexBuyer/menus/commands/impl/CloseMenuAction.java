package me.jetby.treexBuyer.menus.commands.impl;

import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.commands.Action;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CloseMenuAction implements Action {
    @Override
    public void execute(@NotNull Player player, @NotNull String context, Button button) {
        player.getInventory().close();
    }
}