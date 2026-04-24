# Deployment Guide

This document describes how to deploy the `mcp-server-generic-jdbc` in various environments.

## Environment Variables

The application requires the following environment variables to be set for successful operation:

### Database Configuration

| Variable | Required | Description |
| :--- | :--- | :--- |
| `DB_KIND` | Yes | The Quarkus database kind (e.g., `mssql`, `postgresql`, `mysql`, `mariadb`, `oracle`). |
| `DB_URL` | Yes | The full JDBC URL for connecting to your database. |
| `DB_USER` | Yes | The database username. |
| `DB_PASS` | Yes | The database password. |

### MCP Tool Customization (Optional)

You can customize the descriptions of the MCP tools to provide better context to the AI agent. **These descriptions should be as exact as possible regarding the purpose and content of the database** (e.g., "Fetch data from the Financial Transactions database to analyze quarterly revenue"). This allows the agent to formulate more accurate queries based on the specific domain.

| Variable | Description |
| :--- | :--- |
| `MCP_TOOL_LIST_TABLES_DESCRIPTION` | Overrides the description of the `list_tables` tool. |
| `MCP_TOOL_DESCRIBE_TABLE_DESCRIPTION` | Overrides the description of the `describe_table` tool. |
| `MCP_TOOL_EXECUTE_SQL_DESCRIPTION` | Overrides the description of the `execute_sql` tool. |

## Running with Docker

### Using Docker Compose

The deployment artifacts are located in the `deployment/` directory.

1. **Navigate to the deployment folder**:
   ```bash
   cd deployment
   ```

2. **Configure your environment**:
   Copy the example environment file and update it with your database credentials and precise tool descriptions.
   ```bash
   cp .env.example .env
   ```

3. **Start the server**:
   ```bash
   docker compose up -d
   ```

**docker-compose.yml**:
Located at `deployment/docker-compose.yml`.

### Using the pre-built image (docker run)

```bash
docker run -e DB_KIND=postgresql 
           -e DB_URL=jdbc:postgresql://host.docker.internal:5432/mydb 
           -e DB_USER=myuser 
           -e DB_PASS=mypass 
           ghcr.io/harald-landvoigt/mcp-server-generic-jdbc:latest
```

## Running the Native Executable

If you have built the native executable:

```bash
DB_KIND=mssql DB_URL="..." DB_USER="..." DB_PASS="..." ./target/mcp-server-generic-jdbc-1.0.0-SNAPSHOT-runner
```
