package at.landvoigt.mcp.jdbc;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class JdbcMcpResource {

    private static final Logger LOG = Logger.getLogger(JdbcMcpResource.class);

    @Inject
    GenericDatabaseRepository repository;

    @Inject
    SqlValidator sqlValidator;

    public McpResponse<Set<String>> getSchemas() {
        LOG.infof("MCP Tool: getSchemas");
        try {
            return McpResponse.ok(repository.getSchema().keySet());
        } catch (final Exception e) {
            LOG.error("Error in list_tables", e);
            return McpResponse.error("Error listing tables", e);
        }
    }

    public McpResponse<List<String>> getSchema(final String tableName) {
        LOG.infof("MCP Tool: getSchema [table=%s]",  tableName);
        try {
            return McpResponse.ok(repository.getSchema(tableName));
        } catch (final Exception e) {
            LOG.errorf(e, "Error in describe_table [table=%s]", tableName);
            return McpResponse.error("Error getting schema for table " + tableName, e);
        }
    }

    public McpResponse<List<Map<String, Object>>> executeSql(final String query) {
        LOG.infof("AUDIT: MCP Tool: executeSql [query=%s]", query);
        if (!sqlValidator.isValidSelect(query)) {
            LOG.warnf("SECURITY: Blocked invalid execute_sql query: %s", query);
            return McpResponse.error("Invalid query. Only SELECT statements are allowed for security reasons.");
        }
        try {
            return McpResponse.ok(repository.executeQuery(query));
        } catch (final Exception e) {
            LOG.errorf(e, "Error executing query: %s", query);
            return McpResponse.error("Error executing query: " + e.getMessage(), e);
        }
    }
}