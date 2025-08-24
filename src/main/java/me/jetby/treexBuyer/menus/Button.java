package me.jetby.treexBuyer.menus;

import me.jetby.treexBuyer.menus.commands.Command;
import org.bukkit.Material;

import java.util.List;

public record Button(

        String id,
        String displayName,
        List<String> lore,
        int slot,
        int amount,
        int customModelData,
        boolean enchanted,
        boolean sellZone,
        Material material,
        List<Command> commands

) {
}
