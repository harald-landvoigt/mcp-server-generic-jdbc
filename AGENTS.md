# MCP Server: Generic JDBC - Agent Guide

This project is a [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server providing read-only access to various JDBC-compliant databases (PostgreSQL, MySQL, MariaDB, MSSQL, Oracle). It is built with **Java 21** and **Quarkus**.

This document helps AI agents understand the server's tools, response formats, and security rules.

---

## 🛠️ Capabilities (Tools)

AI agents can interact with the connected database through the following tools:

### 1. `list_tables`
- **Description**: Lists all tables currently accessible in the database.
- **Usage Recommendation**: Always call this first when starting a session to discover which data entities are available.

### 2. `describe_table`
- **Description**: Inspects column types, names, and schemas for a specific table.
- **Arguments**:
  - `tableName`: (string, required) The exact name of the table to describe.
- **Usage Recommendation**: Use this to understand column names and exact data types before formulating SQL queries.

### 3. `execute_sql`
- **Description**: Executes a read-only SQL query (`SELECT` or `WITH`).
- **Arguments**:
  - `query`: (string, required) The SQL statement to run.
- **Usage Recommendation**: Use this tool to retrieve records or compute metrics.

---

## 📜 Response Envelope

All three tools return a standard JSON envelope:

### Successful Response
```json
{
  "ok": true,
  "data": <tool-specific payload>,
  "meta": { "timestamp": "2026-06-22T10:00:00Z" }
}
```

### Failed Response
```json
{
  "ok": false,
  "error": {
    "message": "Error description",
    "type": "java.lang.Exception",
    "details": { "cause": null, "exception": "java.lang.Exception" }
  },
  "meta": { "timestamp": "2026-06-22T10:00:00Z" }
}
```

> [!IMPORTANT]
> **Check the `ok` field first**. If `ok` is `false`, inspect the `error` object to understand the issue (e.g. syntax error or security block).

---

## 🔒 Security Restrictions & Validation

The server contains a built-in security validator: [SqlValidator](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/main/java/at/landvoigt/mcp/jdbc/SqlValidator.java).

- **Read-Only Enforced**: Only statements starting with `SELECT` or `WITH` are allowed.
- **No Semicolons Mid-Query**: Mid-query semicolons are blocked to prevent stacked queries (like `SELECT ...; DROP TABLE ...;`). Semicolons are only permitted at the very end of the query string.
- **Blocked Keywords**: Commands containing statements such as `INSERT`, `UPDATE`, `DELETE`, `DROP`, `ALTER`, `TRUNCATE`, `EXECUTE`, `CREATE`, `MERGE` are rejected.
- **Comments/String Safety**: Comments and string literals are stripped before validation, so you can safely write queries containing words like `'DROP'` in comments or string literals.

---

## 📂 Code Reference Links
- [GenericDatabaseRepository](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/main/java/at/landvoigt/mcp/jdbc/GenericDatabaseRepository.java) - Details on how metadata and SQL queries are executed.
- [JdbcMcpResource](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/main/java/at/landvoigt/mcp/jdbc/JdbcMcpResource.java) - The MCP tool interface layer.
- [SqlValidator](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/main/java/at/landvoigt/mcp/jdbc/SqlValidator.java) - Regular expressions validating SQL queries.
