package me.jetby.treexBuyer.menus.requirements;

import lombok.experimental.UtilityClass;
import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.commands.ActionExecutor;
import me.jetby.treexBuyer.menus.commands.ActionRegistry;
import me.jetby.treexBuyer.tools.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static me.jetby.treexBuyer.functions.AutoBuy.isRegularItem;

@UtilityClass
public class ClickRequirement {

    public boolean check(Player player,
                         Requirements requirements, double totalPrice, int totalScore, Button button) {


        if (requirements.type().equalsIgnoreCase("has permission")) {
            return player.hasPermission(requirements.permission());
        }
        if (requirements.type().equalsIgnoreCase("!has permission")) {
            return !player.hasPermission(requirements.permission());
        }
        if (requirements.type().equalsIgnoreCase("string equals")) {
            return requirements.input().equalsIgnoreCase(requirements.output());
        }
        if (requirements.type().equalsIgnoreCase("!string equals")) {
            return !requirements.input().equalsIgnoreCase(requirements.output());
        } else if (requirements.type().equalsIgnoreCase("javascript")) {
            String[] args = requirements.input().split(" ");
            if (args.length < 3) return false;

            args[0] = TextUtil.setPapi(player, args[0]
                    .replace("%sell_pay%", String.valueOf(totalPrice))
                    .replace("%sell_score%", String.valueOf(totalScore))
                    .replace("%item_amount%", items(player, button))
            );
            args[0] = TextUtil.setPapi(player, args[0]
                    .replace("%sell_pay%", String.valueOf(totalPrice))
                    .replace("%sell_score%", String.valueOf(totalScore))
                    .replace("%item_amount%", items(player, button))
            );

            args[2] = TextUtil.setPapi(player, args[2]
                    .replace("%sell_pay%", String.valueOf(totalPrice))
                    .replace("%sell_score%", String.valueOf(totalScore))
                    .replace("%item_amount%", items(player, button))
            );
            args[2] = TextUtil.setPapi(player, args[2]
                    .replace("%sell_pay%", String.valueOf(totalPrice))
                    .replace("%sell_score%", String.valueOf(totalScore))
                    .replace("%item_amount%", items(player, button))
            );

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

    public static void runDenyCommands(Player player, List<String> denyCommands, Button button) {
        ActionExecutor.execute(player, ActionRegistry.transform(denyCommands), button);
    }
}
