# MCP Server: Generic JDBC

This project is a [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server that provides read-only access to various JDBC-compliant databases (PostgreSQL, MySQL, MariaDB, MSSQL, Oracle). It is built with Java 21 and Quarkus.

## Purpose
Allows AI agents to explore database schemas and execute read-only SQL queries to answer questions based on database content.

## Capabilities (Tools)

### `list_tables`
- **Description**: Lists all available tables in the connected database.
- **Usage**: Use this first to understand what data entities are available.

### `describe_table`
- **Description**: Provides the schema (columns, types) for a specific table.
- **Arguments**:
  - `tableName`: The name of the table to describe.
- **Usage**: Use this to understand the structure of a table before writing queries.

### `execute_sql`
- **Description**: Executes a read-only SQL `SELECT` or `WITH` query.
- **Arguments**:
  - `query`: The SQL query to execute.
- **Security**: This tool is restricted to read-only statements. It uses a basic `SqlValidator` to block destructive commands (`INSERT`, `UPDATE`, `DELETE`, `DROP`, etc.).
- **Usage**: Use this to retrieve data to answer specific questions.

## Configuration

The server requires the following environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_KIND` | The database type (e.g., `postgresql`, `mysql`, `mssql`, `mariadb`, `oracle`) | `postgresql` |
| `DB_USER` | Database username | `admin` |
| `DB_PASS` | Database password | `secret` |
| `DB_URL`  | JDBC Connection URL | `jdbc:postgresql://localhost:5432/mydb` |

### Tool Customization (Optional)
The names and descriptions of the tools can be overridden using these properties. **To maximize agent effectiveness, ensure these descriptions precisely define the purpose and content of the database** (e.g., "Query the inventory management system for parts and suppliers").
- `MCP_TOOL_LIST_TABLES_DESCRIPTION`
- `MCP_TOOL_DESCRIBE_TABLE_DESCRIPTION`
- `MCP_TOOL_EXECUTE_SQL_DESCRIPTION`

## Deployment & Execution

### Local Development
```bash
./mvnw quarkus:dev
```

### Building Container
```bash
./mvnw package -Dquarkus.container-image.build=true
```

### Running with Docker Compose
```bash
cd deployment
# Use the provided .env.example as a template
cp .env.example .env 
docker compose up -d
```

### Running with Docker (Direct)
```bash
docker run -i --rm \
  -e DB_KIND=mysql \
  -e DB_USER=root \
  -e DB_PASS=pass \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/test \
  ghcr.io/harald-landvoigt/mcp-server-generic-jdbc:latest
```

## Security Considerations
- **Read-Only Access**: It is strongly recommended to use a database user with read-only permissions at the database level.
- **SQL Validation**: The server performs basic regex-based validation to prevent non-SELECT statements, but this is a secondary defense.
