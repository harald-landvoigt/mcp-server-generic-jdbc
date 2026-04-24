# MCP Server: Generic JDBC

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server implementation providing read-only database access to AI agents via JDBC. 

This server allows LLMs to interact with various databases (PostgreSQL, MySQL, MariaDB, MSSQL, Oracle) to discover schemas and retrieve data.

## Features

- **`list_tables`**: Discover available tables in the database.
- **`describe_table`**: Inspect column names and data types for a specific table.
- **`execute_sql`**: Execute read-only `SELECT` queries to answer complex data-related questions.
- **Security**: Built-in SQL validation to prevent destructive operations (e.g., `DROP`, `DELETE`, `UPDATE`).

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

## Documentation

- [AGENTS.md](./AGENTS.md): Detailed information for AI agents on how to use the provided tools.
- [DEPLOYMENT.md](./DEPLOYMENT.md): Comprehensive guide for different deployment scenarios.

## License

See the project's license for more details.
