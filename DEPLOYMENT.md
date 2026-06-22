# Deployment Guide

This guide details how to build, configure, and deploy the `mcp-server-generic-jdbc` server in development, staging, or production environments.

---

## ⚙️ Environment Variables

The server behaves based on standard environment variables read by the Quarkus configuration system:

### 1. Database Connection Configuration

| Variable | Required | Description | Example |
| :--- | :---: | :--- | :--- |
| `DB_KIND` | **Yes** | Database kind (`postgresql`, `mysql`, `mssql`, `mariadb`, `oracle`). | `postgresql` |
| `DB_URL` | **Yes** | JDBC connection string. | `jdbc:postgresql://localhost:5432/mydb` |
| `DB_USER` | **Yes** | Username used to authenticate database connection. | `db_reader` |
| `DB_PASS` | **Yes** | Password used to authenticate database connection. | `securepassword` |

### 2. MCP Tool Customization (Optional)

These variables change the descriptions of the MCP tools. Crafting specific descriptions makes the server significantly more useful for AI agents because it helps them identify when and how to query the tables.

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `MCP_TOOL_LIST_TABLES_DESCRIPTION` | Customize the list tables tool description. | *List tables in the CRM system...* |
| `MCP_TOOL_DESCRIBE_TABLE_DESCRIPTION` | Customize the describe table tool description. | *Inspect columns of the billing table...* |
| `MCP_TOOL_EXECUTE_SQL_DESCRIPTION` | Customize the query execution tool description. | *Query sales records to compute revenue...* |

---

## 🏎️ Quarkus & JDBC Connection Pool Tuning

Quarkus uses the **Agroal** connection pool. In production, you can tune the connection pool behavior using system properties or environment variables in [application.properties](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/src/main/resources/application.properties):

| Property / Env Variable | Description | Default |
| :--- | :--- | :---: |
| `quarkus.datasource.jdbc.initial-size` | Number of connections created at startup. | `0` |
| `quarkus.datasource.jdbc.min-size` | Minimum connection pool size. | `0` |
| `quarkus.datasource.jdbc.max-size` | Maximum connection pool size. | `20` |
| `quarkus.datasource.jdbc.acquisition-timeout` | Maximum time to wait for a connection before failing. | `5s` |
| `quarkus.datasource.jdbc.idle-removal-interval` | Interval after which idle connections are closed. | `5m` |
| `quarkus.http.port` | Network port to listen on. | `8080` |
| `quarkus.http.host` | Network host interface. | `0.0.0.0` |
| `quarkus.log.level` | Logging level (`INFO`, `DEBUG`, `WARN`, `ERROR`). | `INFO` |

---

## 🐳 Docker Deployment

### 1. Docker Compose
The deployment files are located in the [deployment/](file:///home/hadi/workspace/landvoigt-it-sources/mcp-server/mcp-server-generic-jdbc/deployment/) folder.

To deploy with Docker Compose:
```bash
cd deployment
# Copy environment template
cp .env.example .env
# Edit details inside .env
nano .env
# Start container
docker compose up -d
```

### 2. Pre-Built Container Run
Run the pre-built Docker container directly:
```bash
docker run -d \
  -p 8080:8080 \
  -e DB_KIND=postgresql \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/mydb \
  -e DB_USER=myuser \
  -e DB_PASS=mypass \
  --name mcp-server-jdbc \
  ghcr.io/harald-landvoigt/mcp-server-generic-jdbc:latest
```

---

## 🏗️ Packaging and Building from Source

### 1. Build a Runnable Uber-JAR
Compile and package the project into a runnable JAR file:
```bash
./mvnw package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

### 2. Build Docker Container (Jib)
Build a docker container locally using Jib:
```bash
./mvnw package -Dquarkus.container-image.build=true -DskipTests
```

### 3. Build a Native Executable
Native compilation compiles Java bytecode into a standalone binary using GraalVM. This reduces memory footprint and enables near-instant startup.

```bash
# Requires GraalVM/Mandrel installed locally
./mvnw package -Pnative -DskipTests
```

To build a native executable inside a Docker container (no GraalVM installation needed locally):
```bash
./mvnw package -Pnative -Dquarkus.native.container-build=true -DskipTests
```

Run the built native binary:
```bash
DB_KIND=postgresql DB_URL="..." DB_USER="..." DB_PASS="..." ./target/mcp-server-generic-jdbc-1.0.0-SNAPSHOT-runner
```

---

## 🔒 Security Best Practices

> [!CAUTION]
> AI agents can formulate arbitrary SQL select queries. You must enforce the principle of least privilege.

1. **Read-Only Database Account**: Ensure the database user (`DB_USER`) has read-only privileges inside the target database. Do not use admin or write accounts.
2. **Network Isolation**: Secure the server endpoints behind a firewall. Do not expose the server to the public internet without proper API gateway authentication or reverse proxy configuration.
3. **Encrypted Database Traffic**: Always use SSL connection properties in the `DB_URL` if connecting to a remote database (e.g. `;encrypt=true;trustServerCertificate=true` for SQL Server or `?ssl=true` for PostgreSQL).
4. **Environment Variables**: Never hardcode credentials. Store passwords securely inside environment secrets (like Docker Secrets or Kubernetes Secrets).
