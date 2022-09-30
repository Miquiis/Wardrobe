package me.miquiis.wardrobe.server.database;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.common.Configs;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class Database {

    private MySQL mySQL;

    public Database()
    {
        this.mySQL = new MySQL(Configs.SERVER_CONFIG.host.get(), Configs.SERVER_CONFIG.port.get(), Configs.SERVER_CONFIG.database.get(), Configs.SERVER_CONFIG.username.get(), Configs.SERVER_CONFIG.password.get());
        mySQL.connect();
    }

    public CompletableFuture<Void> saveSkinURL(String skinId, String skinURL, boolean isSlim)
    {
        return mySQL.asyncUpdate(
                String.format(
                         "INSERT INTO skins (id, url, slim) VALUES ('%s', '%s', %s)" +
                                "ON DUPLICATE KEY UPDATE url=VALUES(url), slim=VALUES(slim);",
                        skinId, skinURL, isSlim
                )
        ).thenRunAsync(this::close);
    }

    public CompletableFuture<SkinLocation> fetchSkinLocation(String skinId)
    {
        return mySQL.asyncResult("SELECT * FROM skins WHERE id='" + skinId + "'").handleAsync((resultSet, throwable) -> {
            try
            {
                if (resultSet.next())
                {
                    return new SkinLocation(skinId, resultSet.getString("url"), resultSet.getBoolean("slim"));
                }
            } catch (SQLException e) {
                if (throwable != null) throwable.printStackTrace();
                e.printStackTrace();
            } finally {
                close();
            }
            return SkinLocation.EMPTY;
        });
    }

    public CompletableFuture<String> fetchSkinURL(String skinId)
    {
        return mySQL.asyncResult("SELECT * FROM skins WHERE id='" + skinId + "'").handleAsync((resultSet, throwable) -> {
            try
            {
                if (resultSet.next())
                {
                    return resultSet.getString("url");
                }
            } catch (SQLException e) {
                if (throwable != null) throwable.printStackTrace();
                e.printStackTrace();
            } finally {
                close();
            }
            return "No URL found.";
        });
    }

    private void close()
    {
        if (mySQL != null)
        {
            System.out.println("Disconnect");
            mySQL.disconnect();
        }
    }
}
