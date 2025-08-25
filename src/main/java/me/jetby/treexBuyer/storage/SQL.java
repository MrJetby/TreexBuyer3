package me.jetby.treexBuyer.storage;

import me.jetby.treexBuyer.Main;
import me.jetby.treexBuyer.tools.Logger;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SQL implements Storage {
    private final Main plugin;

    private final boolean mySql;
    private final Connection connection;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>( );

    private static final String CREATE_DATABASE = "CREATE TABLE IF NOT EXISTS players (uuid TEXT NOT NULL, score INTEGER DEFAULT 0, autoBuy BOOLEAN DEFAULT false, autoBuyItems TEXT DEFAULT '', PRIMARY KEY (uuid));";
    private static final String GET_ALL_PLAYERS = "SELECT * FROM players;";
    private static final String INSERT_PLAYER = "INSERT INTO players (uuid, score, autoBuy, autoBuyItems) VALUES (?, ?, ?, ?) ON CONFLICT(uuid) DO UPDATE SET score = ?, autoBuy = ?, autoBuyItems = ?;";
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUser;
    private final String mysqlPassword;

    public SQL(Main plugin) {
        this.plugin = plugin;

        this.mySql = plugin.getCfg( ).getStorageType( ).equalsIgnoreCase("MYSQL".toUpperCase( ));

        this.mysqlHost = plugin.getCfg( ).getHost( );
        this.mysqlPort = plugin.getCfg( ).getPort( );
        this.mysqlDatabase = plugin.getCfg( ).getDatabase( );
        this.mysqlUser = plugin.getCfg( ).getUsername( );
        this.mysqlPassword = plugin.getCfg( ).getPassword( );

        this.connection = connect();
    }


    private static class PlayerData {
        int score = 0;
        boolean autoBuy = false;
        List<String> autoBuyItems = new ArrayList<>( );

        PlayerData(int score, boolean autoBuy, List<String> autoBuyItems) {
            this.score = score;
            this.autoBuy = autoBuy;
            this.autoBuyItems = new ArrayList<>(autoBuyItems);
        }

        PlayerData() {
        }
    }


    public void closeConnection() {
        try {
            connection.close( );
        } catch (SQLException e) {
            Logger.error("Connection close was failed: " + e);
        }
    }

    private Connection connect() {
        try {
            if (mySql) {
                try {
                    Class.forName(MYSQL_DRIVER);
                } catch (ClassNotFoundException e) {
                    throw new SQLException("MySQL driver not found", e);
                }
                return DriverManager.getConnection(
                        "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase + "?useSSL=false",
                        mysqlUser, mysqlPassword);
            } else {
                return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder( ) + "/storage.db");
            }
        } catch (SQLException e) {
            Logger.error(""+e);
        }

        return null;
    }

    @Override
    public boolean load() {
        boolean status = false;
        if (connection!=null) {
            createTables( );
            loadCacheAsync( );
            status = true;
        }
        return status;
    }


    private void createTables() {
        try (Statement statement = connection.createStatement( )) {
            statement.execute(CREATE_DATABASE);
            Logger.success("Таблица игроков успешно создана или уже существует.");
        } catch (SQLException e) {
            Logger.error("Ошибка при создании таблицы игроков: " + e.getMessage( ));
        }
    }

    private void loadCacheAsync() {
        Bukkit.getScheduler( ).runTaskAsynchronously(plugin, () -> {
            long start = System.currentTimeMillis( );
            try (PreparedStatement ps = connection.prepareStatement(GET_ALL_PLAYERS);
                 ResultSet rs = ps.executeQuery( )) {
                while (rs.next( )) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int score = rs.getInt("score");
                    boolean autoBuy = rs.getBoolean("autoBuy");
                    String itemsStr = rs.getString("autoBuyItems");
                    List<String> items = itemsStr == null || itemsStr.isEmpty( ) ? new ArrayList<>( ) : Arrays.asList(itemsStr.split(","));
                    cache.put(uuid, new PlayerData(score, autoBuy, items));
                }
                Logger.success("Данные из storage.db были загружены за " + (System.currentTimeMillis( ) - start) + " мс");
            } catch (SQLException e) {
                Logger.error("Ошибка загрузки кэша: " + e.getMessage( ));
            }
        });
    }

    @Override
    public boolean save() {
        boolean status = false;
        try {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_PLAYER)) {
                for (Map.Entry<UUID, PlayerData> entry : cache.entrySet( )) {
                    UUID uuid = entry.getKey( );
                    PlayerData data = entry.getValue( );
                    String itemsStr = String.join(",", data.autoBuyItems);
                    ps.setString(1, uuid.toString( ));
                    ps.setInt(2, data.score);
                    ps.setBoolean(3, data.autoBuy);
                    ps.setString(4, itemsStr);
                    ps.setInt(5, data.score);
                    ps.setBoolean(6, data.autoBuy);
                    ps.setString(7, itemsStr);
                    ps.addBatch( );
                }
                ps.executeBatch( );
            }

            connection.commit( );
            status = true;
            Logger.success("Кэш сохранён в БД.");
        } catch (SQLException e) {
            Logger.error("Ошибка сохранения кэша: " + e.getMessage( ));
            try {
                connection.rollback( );
            } catch (SQLException ex) {
                Logger.error("Ошибка отката транзакции: " + ex.getMessage( ));
            }
        }

        closeConnection( );
        cache.clear( );
        return status;
    }

    @Override
    public String type() {
        return mySql ? "MYSQL" : "SQLITE";
    }

    @Override
    public boolean playerExists(UUID uuid) {
        return cache.containsKey(uuid);
    }

    @Override
    public void setScore(UUID uuid, int score) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData( ));
        data.score = score;
        scheduleAsyncUpdate(uuid, data);
    }

    @Override
    public int getScore(UUID uuid) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData( ));
        return data.score;
    }

    @Override
    public void setAutoBuyItems(UUID uuid, List<String> items) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData( ));
        data.autoBuyItems = new ArrayList<>(items);
        scheduleAsyncUpdate(uuid, data);
    }

    @Override
    public List<String> getAutoBuyItems(UUID uuid) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData( ));
        return new ArrayList<>(data.autoBuyItems);
    }

    @Override
    public void setAutoBuyStatus(UUID uuid, boolean status) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData( ));
        data.autoBuy = status;
        scheduleAsyncUpdate(uuid, data);
    }

    @Override
    public boolean getAutoBuyStatus(UUID uuid) {
        PlayerData data = cache.computeIfAbsent(uuid, k -> new PlayerData( ));
        return data.autoBuy;
    }

    private void scheduleAsyncUpdate(UUID uuid, PlayerData data) {
        Bukkit.getScheduler( ).runTaskAsynchronously(plugin, () -> {
            String itemsStr = String.join(",", data.autoBuyItems);
            try (PreparedStatement ps = connection.prepareStatement(INSERT_PLAYER)) {
                ps.setString(1, uuid.toString( ));
                ps.setInt(2, data.score);
                ps.setBoolean(3, data.autoBuy);
                ps.setString(4, itemsStr);
                ps.setInt(5, data.score);
                ps.setBoolean(6, data.autoBuy);
                ps.setString(7, itemsStr);
                ps.executeUpdate( );
            } catch (SQLException e) {
                Logger.error("Ошибка обновления игрока " + uuid + ": " + e.getMessage( ));
            }
        });
    }
}
