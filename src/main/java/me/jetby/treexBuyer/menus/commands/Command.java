package me.jetby.treexBuyer.menus.commands;

import me.jetby.treexBuyer.menus.requirements.ClickRequirement;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public record Command(
        boolean anyClick,
        ClickType clickType,
        List<String> actions,
        List<ClickRequirement> requirements

) {
}
