package me.jetby.treexBuyer.menus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.menus.commands.Command;
import me.jetby.treexBuyer.menus.requirements.ClickRequirement;
import me.jetby.treexBuyer.tools.Logger;
import me.jetby.treexBuyer.tools.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;

import java.io.File;
import java.util.*;

@RequiredArgsConstructor
public class Loader {

    @Getter
    private final Map<String, Menu> menus = new HashMap<>();
    @Getter
    private final Map<UUID, JGui> jGui = new HashMap<>();

    private final Main plugin;
    private final File file;

    public void load() {


        File folder = new File(file, "Menu");

        if (!folder.exists() && folder.mkdirs()) {
            String[] defaults = {"mine.yml", "mobs.yml", "seller.yml"};

            for (String name : defaults) {
                File target = new File(folder, name);

                if (!target.exists()) {
                    plugin.saveResource("Menu/" + name, false);
                    Logger.info("Меню Menu/" + name + " создана");
                }

                FileConfiguration config = YamlConfiguration.loadConfiguration(target);
                loadMenu(config.getString("id"), target);
            }
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().endsWith(".yml")) continue;
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                loadMenu(config.getString("id", file.getName().replace(".yml", "")), file);
                Logger.info("Меню Menu/" + config.getString("id") + ".yml загружена");
            }
        }
    }

    private void loadMenu(String menuId, File file) {

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            String title = config.getString("title");
            String type = config.getString("type", "default");
            int size = config.getInt("size", 27);
            String permission = config.getString("open_permission");
            InventoryType inventoryType = InventoryType.valueOf(config.getString("inventory", "CHEST"));
            List<String> openCommands = config.getStringList("open_commands");
            List<Button> buttons = loadButtons(config);

            menus.put(menuId, new Menu(menuId, title, type, size, inventoryType, permission, openCommands, buttons));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Button> loadButtons(FileConfiguration config) {
        List<Button> buttons = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("Items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    String displayName = itemSection.getString("display_name");
                    List<String> lore = itemSection.getStringList("lore");
                    List<Integer> slots = parseSlots(itemSection.get("slot"));
                    int amount = itemSection.getInt("amount", 1);
                    int customModelData = itemSection.getInt("customModelData", 0);
                    boolean enchanted = itemSection.getBoolean("enchanted", false);
                    boolean sellZone = itemSection.getBoolean("sell-zone", false);
                    String defaultMaterial;
                    if (sellZone) {
                        defaultMaterial = "AIR";
                    } else {
                        defaultMaterial = "STONE";
                    }
                    Material material = Material.valueOf(TextUtil.setPapi(null, itemSection.getString("material", defaultMaterial)));
                    for (Integer slot : slots) {
                        buttons.add(new Button(key, displayName, lore, slot, amount, customModelData, enchanted, sellZone, material, loadCommands(itemSection)));
                    }
                }
            }
        }
        return buttons;
    }

    private List<Command> loadCommands(ConfigurationSection itemSection) {
        List<Command> commands = new ArrayList<>();

        if (itemSection.contains("left_click_commands")) {

            commands.add(new Command(false, ClickType.LEFT, itemSection.getStringList("left_click_commands"),
                    requirements(itemSection, "left_click_requirements", ClickType.LEFT, false)));

        }
        if (itemSection.contains("right_click_commands")) {
            commands.add(new Command(false, ClickType.RIGHT, itemSection.getStringList("right_click_commands"),
                    requirements(itemSection, "right_click_requirements", ClickType.RIGHT, false)));

        }
        if (itemSection.contains("shift_left_click_commands")) {

            commands.add(new Command(false, ClickType.SHIFT_LEFT, itemSection.getStringList("shift_left_click_commands"),
                    requirements(itemSection, "shift_left_click_requirements", ClickType.SHIFT_LEFT, false)));

        }
        if (itemSection.contains("shift_right_click_commands")) {
            commands.add(new Command(false, ClickType.SHIFT_RIGHT, itemSection.getStringList("shift_right_click_commands"),
                    requirements(itemSection, "shift_right_click_requirements", ClickType.SHIFT_RIGHT, false)));

        }
        if (itemSection.contains("click_commands")) {
            commands.add(new Command(true, ClickType.UNKNOWN, itemSection.getStringList("click_commands"),
                    requirements(itemSection, "click_requirements", ClickType.UNKNOWN, true)));

        }
        if (itemSection.contains("drop_commands")) {
            commands.add(new Command(false, ClickType.DROP, itemSection.getStringList("drop_commands"),
                    requirements(itemSection, "drop_requirements", ClickType.DROP, false)));
        }

        return commands;
    }


    private List<ClickRequirement> requirements(ConfigurationSection itemSection, String name, ClickType clickType, boolean anyClick) {
        List<ClickRequirement> requirements = new ArrayList<>();
        ConfigurationSection requirementsSection = itemSection.getConfigurationSection(name);
        if (requirementsSection == null) {
            return requirements;
        }
        for (String key : requirementsSection.getKeys(false)) {
            ConfigurationSection section = requirementsSection.getConfigurationSection(key);
            if (section == null) continue;
            requirements.add(new ClickRequirement(anyClick, clickType,
                    section.getString("type"),
                    section.getString("input"),
                    section.getString("output"),
                    section.getString("permission"),
                    section.getStringList("deny_commands")));
        }
        return requirements;
    }

    private List<Integer> parseSlots(Object slotObject) {
        List<Integer> slots = new ArrayList<>();

        if (slotObject instanceof Integer) {
            slots.add((Integer) slotObject);
        } else if (slotObject instanceof String) {
            String slotString = ((String) slotObject).trim();
            slots.addAll(parseSlotString(slotString));
        } else if (slotObject instanceof List<?>) {
            for (Object obj : (List<?>) slotObject) {
                if (obj instanceof Integer) {
                    slots.add((Integer) obj);
                } else if (obj instanceof String) {
                    slots.addAll(parseSlotString((String) obj));
                }
            }
        } else {
            Bukkit.getLogger().warning("Неизвестный формат слотов: " + slotObject);
        }

        return slots;
    }

    private List<Integer> parseSlotString(String slotString) {
        List<Integer> slots = new ArrayList<>();
        if (slotString.contains("-")) {
            try {
                String[] range = slotString.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Ошибка парсинга диапазона слотов: " + slotString);
            }
        } else {
            try {
                slots.add(Integer.parseInt(slotString));
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Ошибка парсинга одиночного слота: " + slotString);
            }
        }
        return slots;
    }
}
