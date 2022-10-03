package me.miquiis.wardrobe.database.server;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.database.LocalCache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class Database {

    private MySQLConnection mySQL;

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
        try {
            mySQL.connect();
            mySQL.query("SELECT * FROM skins_dev LIMIT 1");
            mySQL.getConnection().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getSortKey(WardrobePage.PageSort pageSort, boolean isAscending)
    {
        switch (pageSort)
        {
            case SLIM:
            {
                return "slim " + (isAscending ? "ASC" : "DESC");
            }
            case ALPHABETIC:
            {
                return "name " + (isAscending ? "ASC" : "DESC");
            }
            case LAST_UPDATED:
            {
                return "uid " + (isAscending ? "ASC" : "DESC");
            }
        }
        return "uid " + (isAscending ? "ASC" : "DESC");
    }

    public CompletableFuture<List<SkinLocation>> fetchPage(String searchBar, WardrobePage.PageSort pageSort, boolean isAscending, int startingAt)
    {
        return mySQL.asyncResult("SELECT * FROM skins_dev WHERE uid>=" + startingAt + (!searchBar.isEmpty() ? " AND name LIKE '%" + searchBar + "%'" : "") + " ORDER BY " + getSortKey(pageSort, isAscending) + ";").handleAsync((resultSet, throwable) -> {
            List<SkinLocation> skinLocations = new ArrayList<>();
            try {
                while (resultSet.next())
                {
                    skinLocations.add(new SkinLocation(resultSet.getString("name"), resultSet.getString("url"), resultSet.getBoolean("slim")));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (throwable != null) throwable.printStackTrace();
            }

            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return skinLocations;
        });
    }

    public CompletableFuture<Void> saveSkinURL(String skinId, String skinURL, boolean isSlim)
    {
        return mySQL.asyncUpdate(
                String.format(
                         "INSERT INTO skins_dev (name, url, slim) VALUES ('%s', '%s', %s)" +
                                "ON DUPLICATE KEY UPDATE url=VALUES(url), slim=VALUES(slim);",
                        skinId, skinURL, isSlim
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
        return mySQL.asyncResult("SELECT * FROM skins_dev WHERE name='" + skinId + "'").handleAsync((resultSet, throwable) -> {
            try
            {
                if (resultSet.next())
                {
                    SkinLocation skinLocation = new SkinLocation(skinId, resultSet.getString("url"), resultSet.getBoolean("slim"));
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
