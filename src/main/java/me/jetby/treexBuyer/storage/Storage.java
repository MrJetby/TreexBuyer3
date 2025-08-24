package me.jetby.treexBuyer.storage;

import java.util.List;
import java.util.UUID;

public interface Storage {

    boolean load();
    boolean save(boolean async);
    String type();

    boolean playerExists(UUID uuid);
    void setScore(UUID uuid, int score);
    int getScore(UUID uuid);
    void setAutoBuyItems(UUID uuid, List<String> items);
    List<String> getAutoBuyItems(UUID uuid);
    void setAutoBuyStatus(UUID uuid, boolean status);
    boolean getAutoBuyStatus(UUID uuid);

}
