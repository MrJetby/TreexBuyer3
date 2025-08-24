package me.jetby.treexBuyer.menus;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGuiClickHandler;
import lombok.experimental.UtilityClass;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.configurations.Items;
import me.jetby.treexBuyer.functions.AutoBuy;
import me.jetby.treexBuyer.menus.commands.ActionExecutor;
import me.jetby.treexBuyer.menus.commands.ActionRegistry;
import me.jetby.treexBuyer.menus.commands.Command;
import me.jetby.treexBuyer.menus.requirements.ClickRequirement;
import me.jetby.treexBuyer.menus.requirements.Requirements;
import me.jetby.treexBuyer.tools.Logger;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.jetby.treexBuyer.Main.NAMESPACED_KEY;
import static me.jetby.treexBuyer.Main.df;

@UtilityClass
public class Manager {

    public String check(boolean status) {
        if (status) {
            return Main.getInstance().getCfg().getEnable();
        }
        return Main.getInstance().getCfg().getDisable();
    }

    public AdvancedGuiClickHandler getClickHandler(Button button, Player player, double totalPrice, int totalScores, Main plugin) {
        AdvancedGuiClickHandler advancedGuiClickHandler = (event, controller) -> {

            event.setCancelled(true);

            ClickType clickType = event.getClick();

            Logger.warn("При клике из класса JGui цена: " + df.format(totalPrice));

            for (Command cmd : button.commands()) {
                if (cmd.clickType() == clickType || cmd.anyClick()) {

                    boolean allRequirementsPassed = true;
                    if (!cmd.requirements().isEmpty()) {
                        for (Requirements requirements : cmd.requirements()) {
                            if ((requirements.anyClick() || requirements.clickType() == clickType)) {
                                if (!ClickRequirement.check(
                                        player, requirements, totalPrice, totalScores, button)) {
                                    ClickRequirement.runDenyCommands(player, requirements.deny_commands(), button);
                                    allRequirementsPassed = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (allRequirementsPassed) {
                        List<String> list = new ArrayList<>(cmd.actions());
                        if (plugin.getItems().getItemValues().containsKey(button.material())) {
                            double price = plugin.getItems().getItemValues().get(button.material()).price();
                            list.replaceAll(s -> s.replace("%price%", df.format(price)));
                            list.replaceAll(s -> s.replace("%price_with_coefficient%", String.valueOf(price * plugin.getCoefficient().get(player))));
                            list.replaceAll(s -> s.replace("%auto_sell_toggle_state%", Manager.check(plugin.getStorage().getAutoBuyItems(player.getUniqueId()).contains(button.material().name()))));
                        }
                        list.replaceAll(s -> s.replace("%sell_pay%", df.format(totalPrice)));
                        list.replaceAll(s -> s.replace("%sell_score%", df.format(totalScores)));
                        ActionExecutor.execute(player, ActionRegistry.transform(list), button);
                        break;
                    }
                }
            }
        };
        return advancedGuiClickHandler;
    }

    public void refreshMenu(Player player, JGui jGui) {

        if (!player.getOpenInventory().getTopInventory().equals(jGui.inventory)) return;
        jGui.runTask(() -> {
            jGui.setTotalPrice(0.0);
            jGui.totalScores = 0;

            for (int i : jGui.sellZoneSlots) {

                ItemStack itemStack = jGui.inventory.getItem(i);
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
                if (!AutoBuy.isRegularItem(itemStack)) continue;

                if (itemStack.getItemMeta().getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING))
                    continue;

                if (!jGui.plugin.getItems().getItemValues().containsKey(itemStack.getType())) continue;
                Items.ItemData itemData = jGui.plugin.getItems().getItemValues().get(itemStack.getType());

                double price = itemData.price() * jGui.plugin.getCoefficient().get(player);
                int score = itemData.score();


                jGui.setTotalPrice(jGui.getTotalPrice() + price * itemStack.getAmount());
                jGui.totalScores += score * itemStack.getAmount();
            }

            for (Button button : jGui.menu.buttons()) {
                if (button.sellZone()) continue;

                jGui.getController(button.id() + button.slot()).get().updateItems(wrapper -> {
                    ItemStack itemStack = wrapper.itemStack();
                    if (itemStack.getType().equals(Material.AIR)) return;
                    if (!itemStack.getItemMeta().getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING)) {
                        return;
                    }
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(button.displayName());
                    itemMeta.setLore(button.lore());


                    if (!button.commands().isEmpty()) {
                        for (Command command : button.commands()) {
                            boolean hasCmd = false;
                            for (String cmd : command.actions()) {
                                if (cmd.startsWith("[AUTOBUY_ITEM_TOGGLE]") || cmd.startsWith("[SELL_ITEM]")) {
                                    hasCmd = true;
                                    break;
                                }
                                ;
                            }
                            if (!hasCmd) continue;
                            if (!jGui.plugin.getItems().getItemValues().containsKey(wrapper.itemStack().getType()))
                                continue;
                            if (!itemMeta.getPersistentDataContainer().get(NAMESPACED_KEY, PersistentDataType.STRING).equalsIgnoreCase("menu_priceItem"))
                                continue;
                            wrapper.enchanted(jGui.plugin.getStorage().getAutoBuyItems(player.getUniqueId()).contains(wrapper.itemStack().getType().name()));

                            break;
                        }

                    }
                    itemStack.setItemMeta(itemMeta);
                    wrapper.itemStack(itemStack);
                    wrapper.update((HumanEntity) player);
                });
            }
        }, 1L);
    }

    public @NotNull String getAutoBuyItemToggle(ItemWrapper wrapper, String cmd) {
        String input;
        if (cmd.startsWith("[AUTOBUY_ITEM_TOGGLE] ")) {
            input = cmd.replace("[AUTOBUY_ITEM_TOGGLE] ", "");
        } else if (cmd.startsWith("[AUTOBUY_ITEM_TOGGLE]")) {
            input = cmd.replace("[AUTOBUY_ITEM_TOGGLE]", "");

        } else if (cmd.startsWith("[SELL_ITEM] ")) {
            input = cmd.replace("[SELL_ITEM] ", "");
        } else if (cmd.startsWith("[SELL_ITEM]")) {
            input = cmd.replace("[SELL_ITEM]", "");

        } else {
            input = wrapper.itemStack().getType().name();
        }
        if (input.equalsIgnoreCase("")) {
            input = wrapper.itemStack().getType().name();
        }
        return input;
    }

}
