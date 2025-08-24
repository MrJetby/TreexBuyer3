package me.jetby.treexBuyer.functions;

import lombok.RequiredArgsConstructor;
import me.jetby.treexBuyer.Main;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class Coefficient {

    private final Main plugin;

    public double get(Player player) {

        double defaultCoefficient = plugin.getCfg().getDefaultCoefficient();
        double maxLegalCoefficient = plugin.getCfg().getMaxCoefficient();
        boolean boostersExceptLegal = plugin.getCfg().isBoosters_except_legal_coefficient();
        double playerScore = plugin.getStorage().getScore(player.getUniqueId());
        int scoreStep = plugin.getCfg().getScores();
        double coefficientStep = plugin.getCfg().getCoefficient();
        int multiplierCount = (int) (playerScore / scoreStep);
        double coefficient = defaultCoefficient + multiplierCount * coefficientStep;
        double baseCoefficient = Math.min(coefficient, maxLegalCoefficient);
        baseCoefficient = Math.max(baseCoefficient, defaultCoefficient);
        double boosterCoefficient = 0.0F;

        for (Boost boost : plugin.getCfg().getBoosts().values()) {
            if (boost.permission() != null && player.hasPermission(boost.permission())) {
                boosterCoefficient += boost.coefficient();
            }
        }

        if (boostersExceptLegal) {
            return round(baseCoefficient + boosterCoefficient);
        } else {
            return round(Math.min(baseCoefficient + boosterCoefficient, maxLegalCoefficient));
        }

    }

    private double round(double value) {
        double scale = Math.pow(10, 2);
        return Math.round(value * scale) / scale;
    }

}
