package me.jetby.treexBuyer.menus.commands.impl;

import lombok.RequiredArgsConstructor;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.menus.Button;
import me.jetby.treexBuyer.menus.JGui;
import me.jetby.treexBuyer.menus.Manager;
import me.jetby.treexBuyer.menus.commands.Action;
import me.jetby.treexBuyer.storage.Storage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@RequiredArgsConstructor
public class AutoBuyItemToggleAction implements Action {

    private final Main plugin;
    @Override
    public void execute(@NotNull Player player, @NotNull String context, Button button) {
        Inventory inventory = player.getOpenInventory().getTopInventory();

        if (inventory == plugin.getMenuLoader().getJGui().get(player.getUniqueId()).getInventory()) {
            JGui jGui = plugin.getMenuLoader().getJGui().get(player.getUniqueId());

            String materialUpper;
            if (context.isEmpty()) {
                materialUpper = button.material().name().toUpperCase();
            } else {
                materialUpper = context.toUpperCase();
            }

            List<String> autoBuyItems = plugin.getStorage().getAutoBuyItems(player.getUniqueId());
            if (autoBuyItems.contains(materialUpper)) {
                autoBuyItems.remove(materialUpper);
            } else {
                autoBuyItems.add(materialUpper);
            }
            plugin.getStorage().setAutoBuyItems(player.getUniqueId(), autoBuyItems);
            Manager.refreshMenu(player, jGui);
        }
    }
}