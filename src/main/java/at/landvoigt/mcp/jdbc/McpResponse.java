package at.landvoigt.mcp.jdbc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record McpResponse<T>(
        @JsonProperty("ok") boolean ok,
        @JsonProperty("data") T data,
        @JsonProperty("error") McpError error,
        @JsonProperty("meta") Map<String, Object> meta
) {

    public static <T> McpResponse<T> ok(final T data) {
        return new McpResponse<>(true, data, null, defaultMeta());
    }

    public static <T> McpResponse<T> ok(final T data, final Map<String, Object> meta) {
        return new McpResponse<>(true, data, null, mergeMeta(meta));
    }

    public static <T> McpResponse<T> error(final String message) {
        return new McpResponse<>(false, null, new McpError(message, null, null), defaultMeta());
    }

    public static <T> McpResponse<T> error(final String message, final Throwable t) {
        return new McpResponse<>(false, null, McpError.from(t, message), defaultMeta());
    }

    public static <T> McpResponse<T> error(final McpError err, final Map<String, Object> meta) {
        return new McpResponse<>(false, null, err, mergeMeta(meta));
    }

    private static Map<String, Object> defaultMeta() {
        return Map.of(
                "timestamp", Instant.now().toString()
        );
    }

    private static Map<String, Object> mergeMeta(final Map<String, Object> meta) {
        if (meta == null || meta.isEmpty()) return defaultMeta();
        // If you want to avoid Map.copyOf on older JDKs, replace with new HashMap<>(...)
        final var base = new java.util.HashMap<>(defaultMeta());
        base.putAll(meta);
        return Map.copyOf(base);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record McpError(
            @JsonProperty("message") String message,
            @JsonProperty("type") String type,
            @JsonProperty("details") Object details
    ) {
        public static McpError from(final Throwable t, final String messageOverride) {
            final String msg = (messageOverride != null && !messageOverride.isBlank())
                    ? messageOverride
                    : (t.getMessage() != null ? t.getMessage() : t.toString());

            Map<String, Object> details = new java.util.HashMap<>();
            details.put("cause", t.getCause() != null ? t.getCause().toString() : null);
            details.put("exception", t.getClass().getName());

            return new McpError(
                    msg,
                    t.getClass().getName(),
                    details
            );
        }
    }
}

