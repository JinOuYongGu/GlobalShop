package me.jinou.globalshop.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.jinou.globalshop.GlobalShop;
import me.jinou.globalshop.nms.ItemStackSerializer;
import me.jinou.globalshop.utils.ShopItem;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author 69142
 */
public class DatabaseManager implements IDataManager {
    private static final GlobalShop PLUGIN = GlobalShop.get();
    private static final ItemStackSerializer ITEM_STACK_SERIALIZER = PLUGIN.getItemStackSerializer();
    private static final String TABLE_NAME = GlobalShop.getFileConfig().getString("mysql.table");
    private static HikariDataSource dataSource = null;

    public DatabaseManager() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" +
                GlobalShop.getFileConfig().getString("mysql.host") + ":" +
                GlobalShop.getFileConfig().getString("mysql.port") + "/" +
                GlobalShop.getFileConfig().getString("mysql.database"));
        config.setUsername(GlobalShop.getFileConfig().getString("mysql.username"));
        config.setPassword(GlobalShop.getFileConfig().getString("mysql.password"));
        dataSource = new HikariDataSource(config);

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` (uid INT PRIMARY KEY NOT NULL, owner_name VARCHAR(32) NOT NULL, owner_id VARCHAR(36) NOT NULL, item LONGTEXT NOT NULL, price DOUBLE NOT NULL, Type VARCHAR(16) NOT NULL, time BIGINT NOT NULL);")
                ) {
                    ps.executeUpdate();
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    @Override
    public void addShopItem(final ShopItem shopItem) {
        String base64 = ITEM_STACK_SERIALIZER.toBase64(shopItem.getItemStack());

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + TABLE_NAME + "` (`uid`, `owner_name`, `owner_id`, `item`, `price`, `Type`, `time`) VALUES (?, ?, ?, ?, ?, ?, ?);")) {
                    ps.setInt(1, shopItem.getUid());
                    ps.setString(2, shopItem.getOwnerName());
                    ps.setString(3, shopItem.getOwnerId().toString());
                    ps.setString(4, base64);
                    ps.setDouble(5, shopItem.getPrice());
                    ps.setString(6, shopItem.getType());
                    ps.setLong(7, shopItem.getCreateTime());
                    ps.executeUpdate();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    @Override
    public void removeShopItem(int uid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + TABLE_NAME + "` WHERE uid = ?;")) {
                    ps.setInt(1, uid);
                    ps.executeUpdate();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    // TODO
    @Override
    public void update() {
    }

    @Override
    public ShopItem getShopItem(int uid) {
        ShopItem shopItem = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `" + TABLE_NAME + "` WHERE uid = ?;")) {
            ps.setInt(1, uid);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                shopItem = new ShopItem(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        UUID.fromString(resultSet.getString(3)),
                        ITEM_STACK_SERIALIZER.fromBase64(resultSet.getString(4)),
                        resultSet.getDouble(5),
                        resultSet.getString(6),
                        resultSet.getLong(7)
                );
            }
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return shopItem;
    }

    /**
     * Must be called in an async task
     *
     * @param type     Type of shopItem
     * @param startIdx starting index
     * @param length   end index
     * @return List of shopItems from MySQL database
     */
    @Override
    public List<ShopItem> getShopItems(String type, String filter, int startIdx, int length) {
        String sqlString;
        if ("sell".equalsIgnoreCase(type)) {
            sqlString = "SELECT * FROM `" + TABLE_NAME + "` WHERE `type` = 'sell' && {TIME_LIMIT} ORDER BY {FILTER} LIMIT ?,?;";
        } else if ("buy".equalsIgnoreCase(type)) {
            sqlString = "SELECT * FROM `" + TABLE_NAME + "` WHERE `type` = 'buy' && {TIME_LIMIT} ORDER BY {FILTER} LIMIT ?,?;";
        } else {
            sqlString = "SELECT * FROM `" + TABLE_NAME + "` WHERE {TIME_LIMIT} ORDER BY {FILTER} LIMIT ?,?;";
        }

        String sqlFilter = "time DESC";
        if ("timeDescend".equalsIgnoreCase(filter)) {
            sqlFilter = "time DESC";
        } else if ("timeAscend".equalsIgnoreCase(filter)) {
            sqlFilter = "time ASC";
        } else if ("priceDescend".equalsIgnoreCase(filter)) {
            sqlFilter = "price DESC";
        } else if ("priceAscend".equalsIgnoreCase(filter)) {
            sqlFilter = "price ASC";
        }
        sqlString = sqlString.replace("{FILTER}", sqlFilter);

        long validTime = GlobalShop.getFileConfig().getLong("shop.valid-time-in-mins") * 60 * 1000;
        long expireTime = (System.currentTimeMillis() - validTime);
        String sqlTimeLimit = "`time` > " + expireTime;
        sqlString = sqlString.replace("{TIME_LIMIT}", sqlTimeLimit);

        List<ShopItem> shopItems = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlString)) {
            ps.setInt(1, startIdx);
            ps.setInt(2, length);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ShopItem shopItem = new ShopItem(
                        rs.getInt(1),
                        rs.getString(2),
                        UUID.fromString(rs.getString(3)),
                        ITEM_STACK_SERIALIZER.fromBase64(rs.getString(4)),
                        rs.getDouble(5),
                        rs.getString(6),
                        rs.getLong(7)
                );
                shopItems.add(shopItem);
            }

            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return shopItems;
    }

    @Override
    public List<ShopItem> getPlayerAllShopItems(UUID playerUuid, int startIdx, int length) {
        String sqlString = "SELECT * FROM `" + TABLE_NAME + "` WHERE `owner_id` = ? ORDER BY time DESC LIMIT ?,?;";

        List<ShopItem> shopItems = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlString)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, startIdx);
            ps.setInt(3, length);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ShopItem shopItem = new ShopItem(
                        rs.getInt(1),
                        rs.getString(2),
                        UUID.fromString(rs.getString(3)),
                        ITEM_STACK_SERIALIZER.fromBase64(rs.getString(4)),
                        rs.getDouble(5),
                        rs.getString(6),
                        rs.getLong(7)
                );
                shopItems.add(shopItem);
            }

            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return shopItems;
    }

    @Override
    public int generateUid() {
        int uid = -1;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement findMinPs = connection.prepareStatement("SELECT MIN(uid) FROM `" + TABLE_NAME + "`;");
             PreparedStatement findMaxPs = connection.prepareStatement("SELECT MAX(uid) FROM `" + TABLE_NAME + "`;")
        ) {
            ResultSet minUidRs = findMinPs.executeQuery();
            List<Integer> uids = new ArrayList<>();
            while (minUidRs.next()) {
                uids.add(minUidRs.getInt(1));
            }

            if (uids.size() == 1 && uids.get(0) <= 1) {
                ResultSet maxUidRs = findMaxPs.executeQuery();
                uids.clear();
                while (maxUidRs.next()) {
                    uids.add(maxUidRs.getInt(1));
                }
                if (uids.size() == 1) {
                    uid = uids.get(0) + 1;
                }
                maxUidRs.close();
            } else if (uids.size() == 1 && uids.get(0) > 1) {
                uid = uids.get(0) - 1;
            }
            minUidRs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return uid;
    }
}
