package me.jetby.treexBuyer.menus.requirements;

import lombok.experimental.UtilityClass;
import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.commands.ActionExecutor;
import me.jetby.treexBuyer.menus.commands.ActionRegistry;
import me.jetby.treexBuyer.tools.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.jetby.treexBuyer.Main.df;
import static me.jetby.treexBuyer.functions.AutoBuy.isRegularItem;

@UtilityClass
public class Requirements {

    public boolean check(Player player,
                         ClickRequirement clickRequirement, double totalPrice, int totalScore, Button button) {


        if (clickRequirement.type().equalsIgnoreCase("has permission")) {
            return player.hasPermission(clickRequirement.permission());
        }
        if (clickRequirement.type().equalsIgnoreCase("!has permission")) {
            return !player.hasPermission(clickRequirement.permission());
        }
        if (clickRequirement.type().equalsIgnoreCase("string equals")) {
            return clickRequirement.input().equalsIgnoreCase(clickRequirement.output());
        }
        if (clickRequirement.type().equalsIgnoreCase("!string equals")) {
            return !clickRequirement.input().equalsIgnoreCase(clickRequirement.output());
        } else if (clickRequirement.type().equalsIgnoreCase("javascript")) {
            String[] args = clickRequirement.input().split(" ");
            if (args.length < 3) return false;

            args[0] = setPlaceholders(player, args[0], totalPrice, totalScore, button);
            args[2] = setPlaceholders(player, args[2], totalPrice, totalScore, button);


            try {
                double x = Double.parseDouble(args[0]);
                double x1 = Double.parseDouble(args[2]);

                return switch (args[1]) {
                    case ">" -> x > x1;
                    case ">=" -> x >= x1;
                    case "==" -> x == x1;
                    case "!=" -> x != x1;
                    case "<=" -> x <= x1;
                    case "<" -> x < x1;
                    default -> false;
                };
            } catch (NumberFormatException e) {
                try {
                    String x = args[0];
                    String x1 = args[2];
                    return switch (args[1]) {
                        case "==" -> x.equals(x1);
                        case "!=" -> !x.equals(x1);
                        default -> false;
                    };
                } catch (Exception ex) {
                    return false;
                }
            }
        }
        return false;
    }

    private static String items(Player player, Button button) {
        int amount = 0;
        try {
            Material item = button.material();
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack == null) continue;
                if (itemStack.getType() != item) continue;
                if (!isRegularItem(itemStack)) continue;
                amount += itemStack.getAmount();
            }
        } catch (Exception e) {
            amount = 0;
        }
        return String.valueOf(amount);
    }

    public static void runDenyCommands(Player player, List<String> denyCommands, double totalPrice,
                                       int totalScore, Button button) {
        List<String> commands = new ArrayList<>();
        for (String string : denyCommands) {
            commands.add(setPlaceholders(player, string, totalPrice, totalScore, button));
        }
        ActionExecutor.execute(player, ActionRegistry.transform(commands), button);
    }

    private String setPlaceholders(Player player, String string,
                                   double totalPrice,
                                   int totalScore, Button button) {
        return TextUtil.setPapi(player, string
                .replace("%sell_pay%", df.format(totalPrice))
                .replace("%sell_score%", df.format(totalScore))
                .replace("%item_amount%", items(player, button))
        );
    }
}
