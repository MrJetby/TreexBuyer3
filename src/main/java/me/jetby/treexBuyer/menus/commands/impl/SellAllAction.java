package me.jetby.treexBuyer.menus.commands.impl;

import lombok.RequiredArgsConstructor;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.configurations.Items;
import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.JGui;
import me.jetby.treexBuyer.menus.Manager;
import me.jetby.treexBuyer.menus.commands.Action;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static me.jetby.treexBuyer.Main.NAMESPACED_KEY;
import static me.jetby.treexBuyer.functions.AutoBuy.isRegularItem;

@RequiredArgsConstructor
public class SellAllAction implements Action {

    private final Main plugin;
    @Override
    public void execute(@NotNull Player player, @NotNull String context, Button button) {

        if (player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("treexbuyer.creative.bypass")) {
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();

        if (inventory==plugin.getMenuLoader().getJGui().get(player.getUniqueId()).getInventory()) {
            JGui jGui = plugin.getMenuLoader().getJGui().get(player.getUniqueId());
            double totalPrice = 0.0;
            int totalScores = 0;
            for (int i : jGui.getSellZoneSlots()) {

                ItemStack itemStack = inventory.getItem(i);
                if (itemStack == null) continue;
                if (itemStack.getItemMeta().getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING)) continue;
                if (!isRegularItem(itemStack)) continue;

                if (!plugin.getItems().getItemValues().containsKey(itemStack.getType())) continue;
                Items.ItemData itemData = plugin.getItems().getItemValues().get(itemStack.getType());

                double price = itemData.price()*plugin.getCoefficient().get(player);
                int score = itemData.score();

                inventory.setItem(i, null);

                totalPrice += price * itemStack.getAmount();
                totalScores += score * itemStack.getAmount();

            }
            if (totalPrice>0L) {
                plugin.getEconomy().depositPlayer(player, totalPrice);
            }
            if (totalScores>0) plugin.getStorage().setScore(player.getUniqueId(), plugin.getStorage().getScore(player.getUniqueId())+totalScores);

            Manager.refreshMenu(player, jGui);



        }

    }
}