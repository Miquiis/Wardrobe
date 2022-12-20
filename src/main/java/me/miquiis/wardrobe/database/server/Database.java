package me.miquiis.wardrobe.database.server;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.WardrobeFolder;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.WardrobeTab;
import me.miquiis.wardrobe.database.LocalCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class Database {

    private static final Logger LOGGER = LogManager.getLogger();
    private MySQLConnection mySQL;

    public static final String SKINS_TABLE = "w_skins";
    public static final String FOLDERS_TABLE = "w_folders";

    public Database()
    {
        try {
            this.mySQL = new MySQLConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void firstBoot()
    {
        mySQL.asyncBatch(
                "CREATE TABLE IF NOT EXISTS " + SKINS_TABLE + " (uid int NOT NULL PRIMARY KEY AUTO_INCREMENT, name varchar(255) NOT NULL UNIQUE, url varchar(2048) NOT NULL, folder varchar(255) NOT NULL, slim bool NOT NULL, baby bool NOT NULL)",
                "CREATE TABLE IF NOT EXISTS " + FOLDERS_TABLE + " (uid int NOT NULL PRIMARY KEY AUTO_INCREMENT, name varchar(255) NOT NULL UNIQUE, item_icon varchar(255) NOT NULL)",
                "INSERT INTO " + FOLDERS_TABLE + " (name, item_icon) VALUES ('Main Folder', 'chest') ON DUPLICATE KEY UPDATE name=name"
        );
    }

    private String getSortKey(WardrobePage.PageSort pageSort, boolean isAscending)
    {
        switch (pageSort)
        {
            case SLIM:
            {
                return "slim " + (isAscending ? "DESC" : "ASC");
            }
            case ALPHABETIC:
            {
                return "name " + (isAscending ? "ASC" : "DESC");
            }
            case LAST_UPDATED:
            {
                return "uid " + (isAscending ? "DESC" : "ASC");
            }
        }
        return "uid " + (isAscending ? "DESC" : "ASC");
    }

    public CompletableFuture<Void> deleteSkin(String skinId)
    {
        return mySQL.asyncUpdate(
                String.format(
                        "DELETE FROM " + SKINS_TABLE + " WHERE name='%s'",
                        skinId
                )
        );
    }

    public CompletableFuture<Void> createNewFolder(String folderName, String folderItem)
    {
        return mySQL.asyncUpdate(
                String.format(
                        "INSERT INTO " + FOLDERS_TABLE + " (name, item_icon) VALUES ('%s', '%s')",
                        folderName, folderItem
                )
        );
    }

    public CompletableFuture<Void> updateExistingFolder(String oldFolderName, String newFolderName, String folderItem)
    {
        return mySQL.asyncUpdate(
                String.format(
                        "UPDATE " + FOLDERS_TABLE + " SET name = '%s', item_icon = '%s' WHERE name = '%s';",
                        newFolderName, folderItem, oldFolderName
                )
        );
    }

    public CompletableFuture<Void> deleteFolder(String folderName)
    {
        return mySQL.asyncUpdate(
                String.format(
                        "DELETE FROM " + FOLDERS_TABLE + " WHERE name='%s'",
                        folderName
                )
        );
    }

    public CompletableFuture<List<WardrobeFolder>> fetchFolders(int startingAt)
    {
        return mySQL.asyncResult("SELECT * FROM " + FOLDERS_TABLE + " LIMIT " + startingAt + "," + 6 + ";").handleAsync((resultSet, throwable) -> {
            List<WardrobeFolder> wardrobeFolders = new ArrayList<>();
            try {
                int folderCount = 0;
                while (resultSet.next())
                {
                    wardrobeFolders.add(new WardrobeFolder(resultSet.getString("name"), resultSet.getString("item_icon"), WardrobeTab.DATABASE_WARDROBE, 1 + (startingAt / 4) + folderCount / 5));
                    folderCount++;
                }
            } catch (Exception e) {
                LOGGER.error("Error trying to get ResultSet.");
            }

            try {
                resultSet.close();
            } catch (SQLException ignored) {}

            return wardrobeFolders;
        });
    }

    public CompletableFuture<List<SkinLocation>> fetchPage(String folderName, String searchBar, WardrobePage.PageSort pageSort, boolean isAscending, int startingAt)
    {
        return mySQL.asyncResult("SELECT * FROM " + SKINS_TABLE + " WHERE folder = '" + folderName + "'" + (!searchBar.isEmpty() ? " AND name LIKE '%" + searchBar + "%'" : "") + " ORDER BY " + getSortKey(pageSort, isAscending) + " LIMIT " + startingAt + "," + 16 + startingAt + ";").handleAsync((resultSet, throwable) -> {
            List<SkinLocation> skinLocations = new ArrayList<>();
            try {
                while (resultSet.next())
                {
                    skinLocations.add(new SkinLocation(resultSet.getString("name"), resultSet.getString("url"), resultSet.getBoolean("slim"), resultSet.getBoolean("baby")));
                }
            } catch (Exception e) {
                LOGGER.error("Error trying to get ResultSet.");
            }

            try {
                resultSet.close();
            } catch (SQLException ignored) {}

            return skinLocations;
        });
    }

    public CompletableFuture<Boolean> hasNextPage(String folderName, String searchBar, WardrobePage.PageSort pageSort, boolean isAscending, int startingAt)
    {
        return mySQL.asyncResult("SELECT * FROM " + SKINS_TABLE + " WHERE folder = '" + folderName + "'" + (!searchBar.isEmpty() ? " AND name LIKE '%" + searchBar + "%'" : "") + " ORDER BY " + getSortKey(pageSort, isAscending) + " LIMIT " + startingAt + "," + 16 + startingAt + ";").handleAsync((resultSet, throwable) -> {
            boolean hasNext = false;
            try {
                hasNext = resultSet.next();
            } catch (Exception e) {
                LOGGER.error("Error trying to get ResultSet.");
            }
            try {
                resultSet.close();
            } catch (SQLException ignored) {}
            return hasNext;
        });
    }

    public CompletableFuture<Void> createNewSkin(String skinId, String skinURL, String folderName, boolean isSlim, boolean isBaby)
    {
        return mySQL.asyncUpdate(
                String.format(
                         "INSERT INTO " + SKINS_TABLE + " (name, url, folder, slim, baby) VALUES ('%s', '%s', '%s', %s, %s)" +
                                "ON DUPLICATE KEY UPDATE url=VALUES(url), folder=VALUES(folder), slim=VALUES(slim), baby=VALUES(baby);",
                        skinId, skinURL, folderName, isSlim, isBaby
                )
        );
    }

    public CompletableFuture<Void> updateExistingSkin(String oldSkinId, String skinId, String skinURL, String folderName, boolean isSlim, boolean isBaby)
    {
        return mySQL.asyncUpdate(
                String.format(
                        "UPDATE " + SKINS_TABLE + " SET name = '%s', url = '%s', folder = '%s', slim = %s, baby = %s WHERE name = '%s';",
                        skinId, skinURL, folderName, isSlim, isBaby, oldSkinId
                )
        );
    }

    public CompletableFuture<SkinLocation> fetchSkinLocation(String skinId)
    {
        Predicate<LocalCache<SkinLocation>.Cached> cachedPredicate = cached -> cached.getValue().getSkinId().equals(skinId);
        if (Wardrobe.getInstance().getServerSkinLocationCache().hasCache(cachedPredicate))
        {
            Optional<LocalCache<SkinLocation>.Cached> cached = Wardrobe.getInstance().getServerSkinLocationCache().getCache(cachedPredicate);
            if (cached.isPresent())
            {
                return CompletableFuture.completedFuture(cached.get().getValue());
            }
        }
        return mySQL.asyncResult("SELECT * FROM " + SKINS_TABLE + " WHERE name='" + skinId + "' LIMIT 1").handleAsync((resultSet, throwable) -> {
            try
            {
                if (resultSet.next())
                {
                    SkinLocation skinLocation = new SkinLocation(skinId, resultSet.getString("url"), resultSet.getBoolean("slim"), resultSet.getBoolean("baby"));
                    Wardrobe.getInstance().getServerSkinLocationCache().cache(skinLocation, cachedPredicate);
                    return skinLocation;
                }
            } catch (SQLException e) {
                if (throwable != null) throwable.printStackTrace();
                e.printStackTrace();
            }

            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            SkinLocation skinLocation = new SkinLocation(skinId, "");
            Wardrobe.getInstance().getServerSkinLocationCache().cache(skinLocation, cachedPredicate);
            return skinLocation;
        });
    }

    public CompletableFuture<String> fetchSkinURL(String skinId)
    {
        return fetchSkinLocation(skinId).thenApplyAsync(SkinLocation::getSkinURL);
    }
}
