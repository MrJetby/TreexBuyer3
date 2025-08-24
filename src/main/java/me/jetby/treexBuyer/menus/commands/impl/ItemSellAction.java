package me.jetby.treexBuyer.menus.commands.impl;

import lombok.RequiredArgsConstructor;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.JGui;
import me.jetby.treexBuyer.menus.Manager;
import me.jetby.treexBuyer.menus.commands.Action;
import me.jetby.treexBuyer.tools.Logger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static me.jetby.treexBuyer.Main.df;
import static me.jetby.treexBuyer.functions.AutoBuy.isRegularItem;


@RequiredArgsConstructor
public class ItemSellAction implements Action {

    private final Main plugin;

    @Override
    public void execute(@NotNull Player player, @NotNull String context, Button button) {

        if (player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("treexbuyer.creative.bypass")) {
            return;
        }
        JGui jGui = plugin.getMenuLoader().getJGui().get(player.getUniqueId());

        Material item;
        int amount;
        try {
            item = button.material( );
            try {
                amount = Integer.parseInt(context);
            } catch (NumberFormatException e) {
                amount = 0;
                if (!context.equalsIgnoreCase("all")) return;
                for (ItemStack itemStack : player.getInventory( ).getContents( )) {
                    if (itemStack == null) continue;
                    if (itemStack.getType( ) != item) continue;
                    if (!isRegularItem(itemStack)) continue;
                    amount += itemStack.getAmount( );
                }
            }
        } catch (Exception e) {
            return;
        }

        double price = plugin.getItems( ).getItemValues( ).get(item).price( )*amount;
        price *= plugin.getCoefficient().get(player);

        int score = plugin.getItems( ).getItemValues( ).get(item).score( );

        plugin.getEconomy( ).depositPlayer(player, price);
        plugin.getStorage( ).setScore(player.getUniqueId( ), plugin.getStorage( ).getScore(player.getUniqueId( )) + score);

        player.getInventory().removeItem(new ItemStack(item, amount));

        jGui.setTotalPrice(price);
        jGui.setTotalScores(score);

        jGui.getController(button.id()+button.slot()).get().removeClickHandler(button.slot( ));

        Logger.warn("При клике из класса ItemSellAction способом jGui.getTotalPrice() цена: "+df.format(jGui.getTotalPrice()));

    }
}