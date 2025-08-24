package me.jetby.treexBuyer.menus.commands.impl;

import lombok.RequiredArgsConstructor;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.JGui;
import me.jetby.treexBuyer.menus.Loader;
import me.jetby.treexBuyer.menus.commands.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class OpenMenuAction implements Action {

    private final Main plugin;
    private final Loader menuLoader;

    @Override
    public void execute(@NotNull Player player, @NotNull String context, Button button) {
        Bukkit.getScheduler( ).runTaskLater(plugin, () -> new JGui(plugin.getMenuLoader().getMenus().get(context), plugin, player).open(player), 1L);

    }
}