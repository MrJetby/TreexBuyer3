package me.jetby.treexBuyer.menus;

import org.bukkit.event.inventory.InventoryType;

import java.util.List;

public record Menu(
        String id,
        String title,
        String type,
        int size,
        InventoryType inventoryType,
        String permission,
        List<String> openCommands,
        List<Button> buttons

) { }
