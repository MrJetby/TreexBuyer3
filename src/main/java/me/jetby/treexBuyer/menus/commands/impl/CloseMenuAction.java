package me.jetby.treexBuyer.menus.commands.impl;

import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.commands.Action;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CloseMenuAction implements Action {
    @Override
    public void execute(@Nullable Player player, @NotNull String context, Button button) {
        if (player != null) player.getInventory().close();
    }
}
