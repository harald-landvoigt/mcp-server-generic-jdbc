package at.landvoigt.mcp.jdbc;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GenericDatabaseRepository {

    private static final Logger LOG = Logger.getLogger(GenericDatabaseRepository.class);

    // Injected for Fail-Fast validation of required environment variables
    private final String dbKind;
    private final String dbUser;
    private final String dbPass;
    private final String dbUrl;

    private final AgroalDataSource dataSource;

    @Inject
    public GenericDatabaseRepository(
            @ConfigProperty(name = "DB_KIND") String dbKind,
            @ConfigProperty(name = "DB_USER") String dbUser,
            @ConfigProperty(name = "DB_PASS") String dbPass,
            @ConfigProperty(name = "DB_URL") String dbUrl,
            AgroalDataSource dataSource) {
        this.dbKind = dbKind;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.dbUrl = dbUrl;
        this.dataSource = dataSource;
        LOG.infof("Database connection configured: URL=%s, User=%s", dbUrl, dbUser);
    }

    public Map<String, List<String>> getSchema() throws SQLException {
        final Map<String, List<String>> schema = new HashMap<>();
        try (final Connection connection = dataSource.getConnection()) {
            final DatabaseMetaData metaData = connection.getMetaData();
            try (final ResultSet tables = metaData.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    final String tableName = tables.getString("TABLE_NAME");
                    schema.put(tableName, getColumnsForTable(metaData, connection.getCatalog(), tableName));
                }
            }
        }
        return schema;
    }

    public List<String> getSchema(final String tableName) throws SQLException {
        try (final Connection connection = dataSource.getConnection()) {
            final DatabaseMetaData metaData = connection.getMetaData();
            return getColumnsForTable(metaData, connection.getCatalog(), tableName);
        }
    }

    private List<String> getColumnsForTable(final DatabaseMetaData metaData, final String catalog, final String tableName) throws SQLException {
        final List<String> columns = new ArrayList<>();
        try (final ResultSet tableColumns = metaData.getColumns(null, null, tableName, "%")) {
            while (tableColumns.next()) {
                columns.add(
                        tableColumns.getString("COLUMN_NAME") +
                                ":" +
                                tableColumns.getString("TYPE_NAME") +
                                ":" +
                                tableColumns.getString("DATA_TYPE")
                );
            }
        }
        return columns;
    }

    public List<Map<String, Object>> executeQuery(final String query) throws SQLException {
        final List<Map<String, Object>> result = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(query)) {

            final ResultSetMetaData metaData = resultSet.getMetaData();
            final int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                final Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                result.add(row);
            }
        }
        return result;
    }
}
