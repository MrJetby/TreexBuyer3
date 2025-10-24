
package me.jetby.treexBuyer.menus.commands.impl.standard;

import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.commands.Action;
import me.jetby.treexBuyer.tools.Logger;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Effect implements Action {
    public void execute(@Nullable Player player, @NotNull String context, Button button) {
        if (player != null) {
            String[] args = context.split(";");

            PotionEffectType potionEffectType;
            try {
                if (args.length < 1) {
                    Logger.warn("PotionEffectType is null");
                    return;
                }

                potionEffectType = PotionEffectType.getByName(args[0].toUpperCase());
            } catch (IllegalArgumentException var9) {
                Logger.warn("PotionEffectType " + args[0] + " is not available");
                return;
            }
            if (potionEffectType==null) {
                Logger.warn("PotionEffectType " + args[0] + " is not available");
                return;
            }
            try {
                int duration = args.length > 1 ? Integer.parseInt(args[1]) : 0;
                int strength = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                player.addPotionEffect(new PotionEffect(potionEffectType, duration * 20, strength));
            } catch (NumberFormatException var8) {
                Logger.warn("Strength and duration must be a number");
            }

        }
    }
}
