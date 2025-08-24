package me.jetby.treexBuyer.menus.commands;

import me.jetby.treexBuyer.menus.Button;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Action {
    void execute(@Nullable Player player, @NotNull String context, @Nullable Button button);
}
