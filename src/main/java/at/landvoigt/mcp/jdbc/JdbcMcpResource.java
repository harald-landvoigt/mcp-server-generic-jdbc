package at.landvoigt.mcp.jdbc;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JdbcMcpResource {

    private static final Logger LOG = Logger.getLogger(JdbcMcpResource.class);

    @Inject
    GenericDatabaseRepository repository;

    @Inject
    SqlValidator sqlValidator;

    @Tool(
            name = "${mcp.tool.list_tables.name}",
            description = "${mcp.tool.list_tables.description}")
    public McpResponse<Set<String>> getSchemas() {
        LOG.info("MCP Tool: list_tables");
        try {
            return McpResponse.ok(repository.getSchema().keySet());
        } catch (final Exception e) {
            LOG.error("Error in list_tables", e);
            return McpResponse.error("Error listing tables", e);
        }
    }

    @Tool(
            name = "${mcp.tool.describe_table.name}",
            description = "${mcp.tool.describe_table.description}")
    public McpResponse<List<String>> getSchema(@ToolArg(description = "${mcp.tool.describe_table.arg.tableName.description}") final String tableName) {
        LOG.infof("MCP Tool: describe_table [table=%s]", tableName);
        try {
            return McpResponse.ok(repository.getSchema(tableName));
        } catch (final Exception e) {
            LOG.errorf(e, "Error in describe_table [table=%s]", tableName);
            return McpResponse.error("Error getting schema for table " + tableName, e);
        }
    }

    @Tool(
            name = "${mcp.tool.execute_sql.name}",
            description = "${mcp.tool.execute_sql.description}")
    public McpResponse<List<Map<String, Object>>> executeSql(
            @ToolArg(description = "${mcp.tool.execute_sql.arg.query.description}") final String query
    ) {
        LOG.infof("AUDIT: MCP Tool: execute_sql [query=%s]", query);
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