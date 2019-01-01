package CivcraftRedefined;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class DatabaseCommands {
    private final DatabaseInterface connection;

    private final String insertLocationQuery;
    private final String removeLocationQuery;

    private final String insertTempBanQuery;
    private final String getTempBanQuery;

    public DatabaseCommands() throws SQLException {
        connection = new DatabaseInterface("jdbc:h2:./config/civcraft/database.db");

        prepareTable();

        StringBuilder insertLocationString = new StringBuilder();
        insertLocationString
                .append("REPLACE INTO ")
                .append("orelocations")
                .append(" (")
                .append("loc")
                .append(") VALUES (?)");

        StringBuilder removeLocationString = new StringBuilder();
        removeLocationString
                .append("DELETE FROM ")
                .append("orelocations")
                .append(" WHERE ")
                .append(" loc = ?");

        StringBuilder insertTempBanString = new StringBuilder();
        insertTempBanString
                .append("REPLACE INTO ")
                .append("tempbans")
                .append(" (")
                .append("uuid, time")
                .append(") VALUES (?, ?)");

        StringBuilder getTempBanString = new StringBuilder();
        getTempBanString
                .append("SELECT ")
                .append("time")
                .append(" FROM ")
                .append("tempbans")
                .append(" WHERE ")
                .append("uuid")
                .append(" = ? LIMIT 1");

        insertLocationQuery = insertLocationString.toString();
        removeLocationQuery = removeLocationString.toString();
        insertTempBanQuery = insertTempBanString.toString();
        getTempBanQuery = getTempBanString.toString();
    }

    public void storeOreMapping(Location<World> loc) {
        String store = LocationSerialization.serialize(loc);
        try (PreparedStatement insertLocation = connection.getPreparedStatement(insertLocationQuery);
             Connection connection = insertLocation.getConnection()) {
            civcraftRedefined.getInstance().getLogger().debug("Saving location of: " + loc.getPosition().toString());

            insertLocation.setString(1, store);

            insertLocation.executeUpdate();
            insertLocation.clearParameters();
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not save location of: " + loc.getPosition().toString(), e);
        }
    }

    public void removeOreMapping(Location<World> loc) {
        String locString = LocationSerialization.serialize(loc);
        try (PreparedStatement removeLocation = connection.getPreparedStatement(removeLocationQuery);
             Connection connection = removeLocation.getConnection()) {
            civcraftRedefined.getInstance().getLogger().debug("Removing location of: " + loc.getPosition().toString());

            removeLocation.setString(1, locString);

            removeLocation.executeUpdate();
            removeLocation.clearParameters();
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not remove location of: " + loc.getPosition().toString(), e);
        }
    }

    public void initOreRevert() {
        try (PreparedStatement pullLocs = connection.getPreparedStatement("SELECT * FROM orelocations");
             Connection connection = pullLocs.getConnection()) {


            try (ResultSet result = pullLocs.executeQuery()) {
                pullLocs.clearParameters();
                while (result.next()) {
                    if (Optional.of(result.getString(1)).get() != null)
                        functions.revertBlock(LocationSerialization.deserialize(result.getString(1)));
                }
            }
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not pull locations on startup.", e);
        }
        try (PreparedStatement deleteData = connection.getPreparedStatement("DELETE FROM orelocations");
             Connection connection = deleteData.getConnection()) {


            deleteData.execute();
            deleteData.clearParameters();
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not pull locations on startup.", e);
        }
    }

    public void tempBanUser(Player player, Timestamp timeBan) {
        byte[] store = DataTypes.getBytesFromUUID(player.getUniqueId());
        try (PreparedStatement insertBan = connection.getPreparedStatement(insertTempBanQuery);
             Connection connection = insertBan.getConnection()) {
            civcraftRedefined.getInstance().getLogger().debug("Saving tempban for: " + player.getName() + ", to be unbanned at " + timeBan);

            insertBan.setBytes(1, store);
            insertBan.setTimestamp(2, timeBan);

            insertBan.executeUpdate();
            insertBan.clearParameters();
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not set tempban. ", e);
        }
    }

    public Timestamp userBanCheck(UUID player) {
        byte[] uuid = DataTypes.getBytesFromUUID(player);

        try (PreparedStatement checkBan = connection.getPreparedStatement(getTempBanQuery);
             Connection connection = checkBan.getConnection()) {

            checkBan.setBytes(1, uuid);

            try (ResultSet result = checkBan.executeQuery()) {
                checkBan.clearParameters();

                if (result.next()) return result.getTimestamp(1);
                else return null;
            }
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not load tempban for player " + player, e);

            return null;
        }
    }

    public void unbanPlayer(UUID player) {
        byte[] uuid = DataTypes.getBytesFromUUID(player);

        try (PreparedStatement delBan = connection.getPreparedStatement("DELETE FROM tempbans WHERE uuid = ?");
             Connection connection = delBan.getConnection()) {

            delBan.setBytes(1, uuid);

            delBan.execute();
            delBan.clearParameters();
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not remove player from banlist.", e);
        }
    }

    public void initUnBanExpired() {
        try (PreparedStatement delBans = connection.getPreparedStatement("DELETE FROM tempbans WHERE time < NOW()");
             Connection connection = delBans.getConnection()) {

            delBans.execute();
            delBans.clearParameters();
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not remove expired bans on startup.", e);
        }
    }

    private void prepareTable() {
        try {
            StringBuilder createTable = new StringBuilder();

            createTable
                    .append("CREATE TABLE IF NOT EXISTS ")
                    .append("orelocations")
                    .append(" (")
                    .append("loc")
                    .append(" VARCHAR(100) NOT NULL, PRIMARY KEY (")
                    .append("loc")
                    .append(")) DEFAULT CHARSET=utf8");

            connection.executeStatement(createTable.toString());

            civcraftRedefined.getInstance().getLogger().debug("Created table orelocations");
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not create table!", e);
        }

        try {
            StringBuilder createTable = new StringBuilder();

            createTable
                    .append("CREATE TABLE IF NOT EXISTS ")
                    .append("tempbans")
                    .append(" (")
                    .append("uuid")
                    .append(" BINARY(16) NOT NULL, time TIMESTAMP, PRIMARY KEY (")
                    .append("uuid")
                    .append(")) DEFAULT CHARSET=utf8");

            connection.executeStatement(createTable.toString());

            civcraftRedefined.getInstance().getLogger().debug("Created table tempbans");
        } catch (SQLException e) {
            civcraftRedefined.getInstance().getLogger().error("Could not create table!", e);
        }
    }
}
