package at.landvoigt.mcp.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mcp.server.ToolManager;
import io.quarkiverse.mcp.server.ToolResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class McpToolRegistrar {

    @Inject
    ToolManager toolManager;

    @Inject
    JdbcMcpResource resource;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "mcp.tool.list_tables.name")
    String listTablesName;
    @ConfigProperty(name = "mcp.tool.list_tables.description")
    String listTablesDesc;

    @ConfigProperty(name = "mcp.tool.describe_table.name")
    String describeTableName;
    @ConfigProperty(name = "mcp.tool.describe_table.description")
    String describeTableDesc;
    @ConfigProperty(name = "mcp.tool.describe_table.arg.tableName.description")
    String describeTableArgDesc;

    @ConfigProperty(name = "mcp.tool.execute_sql.name")
    String executeSqlName;
    @ConfigProperty(name = "mcp.tool.execute_sql.description")
    String executeSqlDesc;
    @ConfigProperty(name = "mcp.tool.execute_sql.arg.query.description")
    String executeSqlArgDesc;

    void registerTools(@Observes StartupEvent event) {
        toolManager.newTool(listTablesName)
                .setDescription(listTablesDesc)
                .setHandler(args -> toToolResponse(resource.getSchemas()), true)
                .register();

        toolManager.newTool(describeTableName)
                .setDescription(describeTableDesc)
                .addArgument("tableName", describeTableArgDesc, true, String.class)
                .setHandler(args -> toToolResponse(
                        resource.getSchema((String) args.args().get("tableName"))), true)
                .register();

        toolManager.newTool(executeSqlName)
                .setDescription(executeSqlDesc)
                .addArgument("query", executeSqlArgDesc, true, String.class)
                .setHandler(args -> toToolResponse(
                        resource.executeSql((String) args.args().get("query"))), true)
                .register();
    }

    private ToolResponse toToolResponse(McpResponse<?> response) {
        try {
            return ToolResponse.success(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            return ToolResponse.error("Serialization error: " + e.getMessage());
        }
    }
}
