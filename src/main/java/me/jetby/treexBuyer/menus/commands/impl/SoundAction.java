package me.jetby.treexBuyer.menus.commands.impl;

import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.commands.Action;
import me.jetby.treexBuyer.tools.Logger;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundAction implements Action {
    @Override
    public void execute(@Nullable Player player, @NotNull String context, Button button) {
        if (player == null) return;

        var args = context.split(";");
        Sound sound;
        try {
            if (args.length >= 1) {
                sound = Sound.valueOf(args[0].toUpperCase());
            } else {
                Logger.warn("Sound is null");
                return;
            }
        } catch (IllegalArgumentException e) {
            Logger.warn("Sound " + args[0] + " is not available");
            return;
        }

        try {
            float volume = args.length > 1 ? Float.parseFloat(args[1]) : 1;
            float pitch = args.length > 2 ? Float.parseFloat(args[2]) : 1;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (NumberFormatException e) {
            Logger.warn("Volume and pitch must be a number");
        }
    }
}