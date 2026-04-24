package at.landvoigt.mcp.jdbc;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.regex.Pattern;

@ApplicationScoped
public class SqlValidator {

    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*(SELECT|WITH)\\b.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // Keywords that are absolutely forbidden in a read-only context.
    // We use \b to ensure we match whole words only.
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
            "\\b(INSERT|UPDATE|DELETE|DROP|ALTER|TRUNCATE|EXEC|EXECUTE|CREATE|GRANT|REVOKE|MERGE|REPLACE)\\b", 
            Pattern.CASE_INSENSITIVE
    );

    // Regex to identify SQL comments and string literals
    private static final Pattern SQL_COMMENTS_STRINGS = Pattern.compile(
            "(--.*?$)|(/\\*.*?\\*/)|('([^'\\\\]|\\\\.)*')|(\"([^\"\\\\]|\\\\.)*\")",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );

    /**
     * Validates if the query is a safe SELECT statement.
     */
    public boolean isValidSelect(String query) {
        if (query == null || query.isBlank()) {
            return false;
        }

        String trimmedQuery = query.trim();

        // 1. Basic check: Must start with SELECT or WITH
        if (!SELECT_PATTERN.matcher(trimmedQuery).matches()) {
            return false;
        }

        // 2. Multi-statement protection: 
        // We forbid semicolons unless they are at the very end of the query.
        // This prevents "SELECT ...; DROP TABLE ..." style injections.
        if (trimmedQuery.contains(";")) {
            String withoutTrailingSemicolon = trimmedQuery.replaceAll(";\\s*$", "");
            if (withoutTrailingSemicolon.contains(";")) {
                return false;
            }
        }

        // 3. Granular Validation:
        // We strip comments and string literals to avoid false positives 
        // (e.g., the word 'DROP' inside a comment or a user's name).
        String sanitizedQuery = SQL_COMMENTS_STRINGS.matcher(trimmedQuery).replaceAll(" ");

        // 4. Check for forbidden keywords in the sanitized query
        if (FORBIDDEN_PATTERN.matcher(sanitizedQuery).find()) {
            return false;
        }

        return true;
    }
}
