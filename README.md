# MCP Server: Generic JDBC

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server implementation providing read-only database access to AI agents via JDBC. 

This server allows LLMs to interact with various databases (PostgreSQL, MySQL, MariaDB, MSSQL, Oracle) to discover schemas and retrieve data.

## Available Tools

The server exposes three MCP tools:

### `list_tables`
- **Arguments**: none
- **Returns**: Array of table name strings
- **Use**: First call — discover what entities exist in the database.

### `describe_table`
- **Argument**: `tableName` (string, required) — exact table name as returned by `list_tables`
- **Returns**: Array of strings in the format `COLUMN_NAME:TYPE_NAME:DATA_TYPE`
- **Use**: Understand column names and types before writing a query.

### `execute_sql`
- **Argument**: `query` (string, required) — a SQL `SELECT` or `WITH` statement
- **Returns**: Array of row objects (each row is a map of column name → value)
- **Security**: Only `SELECT` and `WITH` are accepted. `INSERT`, `UPDATE`, `DELETE`, `DROP`, `ALTER`, `TRUNCATE` and mid-query semicolons are blocked.

## Prerequisites

- Java 21+
- A JDBC-compatible database
- (Optional) Docker and Docker Compose

## Configuration

The application is configured via environment variables:

| Variable | Description |
| :--- | :--- |
| `DB_KIND` | Database type (e.g., `postgresql`, `mysql`, `mssql`, `mariadb`, `oracle`) |
| `DB_URL` | JDBC Connection URL (e.g., `jdbc:postgresql://localhost:5432/db`) |
| `DB_USER` | Database username |
| `DB_PASS` | Database password |

### MCP Tool Customization (Optional)

You can customize how the tools appear to the AI agent by setting these variables. **It is highly recommended to make these descriptions as exact as possible**, specifically describing the purpose and content of your database (e.g., "Access the CRM database containing customer contacts and sales history") to help the agent understand the available data.

| Variable | Default Description |
| :--- | :--- |
| `MCP_TOOL_LIST_TABLES_DESCRIPTION` | List all available database tables... |
| `MCP_TOOL_DESCRIBE_TABLE_DESCRIPTION` | Get the schema/DDL for a specific database table... |
| `MCP_TOOL_EXECUTE_SQL_DESCRIPTION` | Run a read-only SQL SELECT query... |

## Getting Started

### 1. Development Mode

Run the application in development mode with live coding enabled:

```bash
./mvnw quarkus:dev
```

### 2. Running with Docker Compose

The easiest way to run the server is using the configuration in the `deployment/` folder:

```bash
cd deployment
# Copy the example env file and edit it
cp .env.example .env
# Start the server
docker compose up -d
```

### 3. Packaging & Running

Package the application as a runnable JAR:

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

## Connecting to the MCP Server

Once the server is running (default port `8080`), two transport endpoints are available:

| Endpoint | Transport | MCP Spec |
| :--- | :--- | :--- |
| `http://localhost:8080/mcp` | Streamable HTTP | 2025-03-26 (current) |
| `http://localhost:8080/mcp/sse` | SSE | 2024-11-05 (legacy) |

Adjust `localhost:8080` to match the host and port where the server is deployed.

### Claude Desktop

Add the following to your Claude Desktop configuration file:
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "jdbc": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

### Other MCP clients

Use the streamable HTTP endpoint `/mcp` for clients that support the current MCP spec (Claude Desktop, Cursor, VS Code + Continue, etc.). Fall back to `/mcp/sse` for clients that only support the legacy SSE transport.

---

## Documentation

- [AGENTS.md](./AGENTS.md): Detailed information for AI agents on how to use the provided tools.
- [DEPLOYMENT.md](./DEPLOYMENT.md): Comprehensive guide for different deployment scenarios.

## License

See the project's license for more details.
