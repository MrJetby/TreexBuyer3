package me.jetby.treexBuyer.menus.commands;

import lombok.Getter;
import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.menus.commands.impl.*;

@Getter
public enum ActionType {

    MESSAGE(new MessageAction()),
    SOUND(new SoundAction()),
    ACTIONBAR(new ActionBarAction()),
    TITLE(new TitleAction()),
    CONSOLE(new ConsoleAction()),
    PLAYER(new PlayerAction()),
    BROADCASTMESSAGE(new BroadcastMessageAction()),
    BROADCAST_MESSAGE(new BroadcastMessageAction()),
    BROADCASTSOUND(new BroadcastSoundAction()),
    BROADCAST_SOUND(new BroadcastSoundAction()),
    BROADCASTTITLE(new BroadcastTitleAction()),
    BROADCAST_TITLE(new BroadcastTitleAction()),
    BROADCASTACTIONBAR(new BroadcastActionBarAction()),
    CLOSE(new CloseMenuAction()),
    SELL_ITEM(new ItemSellAction(Main.getInstance())),
    SELL_ALL(new SellAllAction(Main.getInstance())),
    ENABLE_ALL(new EnableALLAction(Main.getInstance())),
    DISABLE_ALL(new DisableALLAction(Main.getInstance())),
    AUTOBUY_STATUS_TOGGLE(new AutoBuyAction(Main.getInstance())),
    AUTOBUY_ITEM_TOGGLE(new AutoBuyItemToggleAction(Main.getInstance())),
    OPEN_MENU(new OpenMenuAction(Main.getInstance(), Main.getInstance().getMenuLoader())),
    BROADCAST_ACTIONBAR(new BroadcastActionBarAction());

    private final Action action;

    ActionType(Action action) {
        this.action = action;
    }
}