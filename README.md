# MCP Server: Generic JDBC

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server implementation providing read-only database access to AI agents via JDBC. This server is built using **Java 21** and the **Quarkus** framework.

This server allows LLMs to interact with various databases (PostgreSQL, MySQL, MariaDB, MSSQL, Oracle) to discover schemas and retrieve data to answer user questions.

> [!NOTE]
> The server performs basic regex-based validation to ensure only read-only `SELECT` or `WITH` queries are executed.

---

## 🛠️ Available Tools

The server exposes three core MCP tools through the [McpToolRegistrar](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/main/java/at/landvoigt/mcp/jdbc/McpToolRegistrar.java):

### 1. `list_tables`
- **Arguments**: None
- **Returns**: Array of table name strings.
- **Use Case**: Discover what database tables exist (e.g., `contacts`, `orders`, `products`).

### 2. `describe_table`
- **Argument**: `tableName` (string, required) — exact table name.
- **Returns**: Column schemas in the format `COLUMN_NAME:TYPE_NAME:DATA_TYPE`.
- **Use Case**: Inspect columns, keys, and types of a table before formulating queries.

### 3. `execute_sql`
- **Argument**: `query` (string, required) — a read-only SQL query (`SELECT` or `WITH` statement).
- **Returns**: Array of rows (each row mapped as column name → value).
- **Use Case**: Query data dynamically to answer specific business or analytical questions.

---

## ⚙️ Configuration

The application is configured using environment variables (defined in [.env](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/.env) or system environment):

| Variable | Required | Description | Default / Example |
| :--- | :---: | :--- | :--- |
| `DB_KIND` | **Yes** | Database type (e.g., `postgresql`, `mysql`, `mssql`, `mariadb`, `oracle`) | `postgresql` |
| `DB_URL` | **Yes** | JDBC Connection URL | `jdbc:postgresql://localhost:5432/mydb` |
| `DB_USER` | **Yes** | Database username | `admin` |
| `DB_PASS` | **Yes** | Database password | `secret` |

### MCP Tool Customization (Optional)

To maximize LLM capability, you can customize tool descriptions to tell the model exactly what information is in your specific database (e.g., *"Access the CRM database containing customer contacts"*).

| Variable | Default Description |
| :--- | :--- |
| `MCP_TOOL_LIST_TABLES_DESCRIPTION` | List all available database tables... |
| `MCP_TOOL_DESCRIBE_TABLE_DESCRIPTION` | Get the schema/DDL for a specific database table... |
| `MCP_TOOL_EXECUTE_SQL_DESCRIPTION` | Run a read-only SQL SELECT query... |

---

## 🚀 Getting Started

### Prerequisites
- **Java 21+** installed
- **Docker** and **Docker Compose** (optional, for containerized run)

### Option A: Quick Start with Public RNA Database (No Setup Needed)
The project comes pre-configured with a public read-only database of RNA sequences in [.env](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/.env).

1. Copy the root `.env` configuration (if not already loaded):
   ```bash
   # Make sure DB configuration is read from .env
   ./mvnw quarkus:dev
   ```
2. The server will start locally and connect to the public RNA database.

### Option B: Local Development Mode
Run the Quarkus development server with live-reloading:
```bash
./mvnw quarkus:dev
```

### Option C: Run using Docker Compose
Deploy the server in containerized mode using the configuration in the [deployment/](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/deployment/) folder:
```bash
cd deployment
cp .env.example .env
# Edit the .env with your specific settings
docker compose up -d
```

---

## 🔍 Security & SQL Validation

To prevent malicious actions (like SQL injections, dropped tables, or data edits), all queries processed through the `execute_sql` tool are validated by [SqlValidator](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/main/java/at/landvoigt/mcp/jdbc/SqlValidator.java):

1. **Structure Check**: The query must start with `SELECT` or `WITH`.
2. **Stacked Queries Blocked**: Mid-query semicolons (`;`) are strictly forbidden to prevent stacked injections (e.g., `SELECT * FROM users; DROP TABLE users;`).
3. **Comment/String Stripping**: Before keyword matching, strings and comments are removed to prevent false positives and bypasses.
4. **Forbidden Keywords**: Queries containing keywords like `DROP`, `DELETE`, `UPDATE`, `ALTER`, `TRUNCATE`, `EXECUTE` are blocked.

> [!WARNING]
> While [SqlValidator](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/main/java/at/landvoigt/mcp/jdbc/SqlValidator.java) provides robust checks, you should **always** configure the database user (`DB_USER`) with read-only permissions at the database level.

---

## 🧪 Testing

The project includes unit tests for SQL validation logic inside [SqlValidatorTest](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/test/java/at/landvoigt/mcp/jdbc/SqlValidatorTest.java).

Run the tests using the Maven wrapper:
```bash
./mvnw test
```

---

## 🔗 Connecting to the MCP Server

Once running (default port `8080`), two HTTP-based transports are exposed:

| Endpoint | Transport Type | MCP Spec Version | Status |
| :--- | :--- | :--- | :--- |
| `http://localhost:8080/mcp` | Streamable HTTP | 2025-03-26 | **Recommended** |
| `http://localhost:8080/mcp/sse` | SSE (Server-Sent Events) | 2024-11-05 | Legacy / Fallback |

### Configuration for Claude Desktop
Add the following snippet to your Claude Desktop config file:
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "generic-jdbc": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

---

## 📚 Project Documentation Links
- [DEPLOYMENT.md](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/DEPLOYMENT.md) - Full guide on production deployment, Agroal connection pools, and container builds.
- [AGENTS.md](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/AGENTS.md) - Instructions for AI agents utilizing the tools.
