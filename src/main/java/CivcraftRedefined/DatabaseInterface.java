package CivcraftRedefined;


import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.*;

public class DatabaseInterface {

    private static final SqlService sql = Sponge.getServiceManager().provide(SqlService.class).get();

    private static String connectionURL;

    protected DatabaseInterface(String connectionURL) throws SQLException {
        DatabaseInterface.connectionURL = connectionURL;


        connect();
    }

    protected static DataSource getDataSource(String jdbcUrl) throws SQLException {
        return sql.getDataSource(jdbcUrl);
    }

    private void connect() throws SQLException {
        civcraftRedefined.getInstance().getLogger()
                .debug("Connecting to: " + connectionURL.replaceFirst(":[^:]*@", ":*****@"));

        // Verify initial connection
        getDataSource();
    }

    private DataSource getDataSource() throws SQLException {
        return getDataSource(connectionURL);
    }

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public Statement getStatement() throws SQLException {
        return getConnection().createStatement();
    }

    public PreparedStatement getPreparedStatement(String statement) throws SQLException {
        civcraftRedefined.getInstance().getLogger().debug("Preparing statement: " + statement);

        return getConnection().prepareStatement(statement);
    }

    public ResultSet executeQuery(String query) throws SQLException {
        try (Statement statement = getStatement();
             Connection connection = statement.getConnection()) {
            return statement.executeQuery(query);
        }
    }

    public boolean executeStatement(String query) throws SQLException {
        try (Statement statement = getStatement();
             Connection connection = statement.getConnection()) {
            return statement.execute(query);
        }
    }

    public int executeUpdate(String query) throws SQLException {
        try (Statement statement = getStatement();
             Connection connection = statement.getConnection()) {
            return statement.executeUpdate(query);
        }
    }
}
