package at.landvoigt.mcp.jdbc;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class SqlValidatorTest {

    @Inject
    SqlValidator sqlValidator;

    @Test
    public void testValidSelect() {
        Assertions.assertTrue(sqlValidator.isValidSelect("SELECT * FROM users"));
        Assertions.assertTrue(sqlValidator.isValidSelect("  select id, name FROM users WHERE age > 18  "));
        Assertions.assertTrue(sqlValidator.isValidSelect("WITH sales AS (SELECT * FROM orders) SELECT * FROM sales"));
    }

    @Test
    public void testInvalidSelect() {
        // Must start with SELECT or WITH
        Assertions.assertFalse(sqlValidator.isValidSelect("INSERT INTO users (name) VALUES ('John')"));
        Assertions.assertFalse(sqlValidator.isValidSelect("UPDATE users SET name = 'John'"));
        Assertions.assertFalse(sqlValidator.isValidSelect("DELETE FROM users"));
        Assertions.assertFalse(sqlValidator.isValidSelect("DROP TABLE users"));
        Assertions.assertFalse(sqlValidator.isValidSelect("ALTER TABLE users ADD COLUMN age INT"));
        Assertions.assertFalse(sqlValidator.isValidSelect("TRUNCATE TABLE users"));
        Assertions.assertFalse(sqlValidator.isValidSelect(""));
        Assertions.assertFalse(sqlValidator.isValidSelect(null));
    }

    @Test
    public void testSemicolonInjection() {
        // Trailing semicolon is allowed
        Assertions.assertTrue(sqlValidator.isValidSelect("SELECT * FROM users;"));
        Assertions.assertTrue(sqlValidator.isValidSelect("SELECT * FROM users;  "));
        
        // Mid-query semicolon is blocked to prevent stacked queries
        Assertions.assertFalse(sqlValidator.isValidSelect("SELECT * FROM users; DROP TABLE users;"));
        Assertions.assertFalse(sqlValidator.isValidSelect("SELECT * FROM users; SELECT * FROM profiles"));
    }

    @Test
    public void testForbiddenKeywordsInCommentsOrStrings() {
        // Forbidden keywords inside comments should be sanitized and allowed
        Assertions.assertTrue(sqlValidator.isValidSelect("SELECT * FROM users -- do not drop this table"));
        Assertions.assertTrue(sqlValidator.isValidSelect("SELECT * FROM users /* alter column */ WHERE id = 1"));
        
        // Forbidden keywords inside string literals should be sanitized and allowed
        Assertions.assertTrue(sqlValidator.isValidSelect("SELECT * FROM users WHERE name = 'DROP'"));
        Assertions.assertTrue(sqlValidator.isValidSelect("SELECT * FROM users WHERE name = 'alter'"));
    }

    @Test
    public void testForbiddenKeywordsInQueryText() {
        // Forbidden keywords in select or where clauses should be blocked
        Assertions.assertFalse(sqlValidator.isValidSelect("SELECT DROP FROM users"));
        Assertions.assertFalse(sqlValidator.isValidSelect("SELECT * FROM users WHERE name = 'x' OR EXISTS (DROP TABLE profiles)"));
        Assertions.assertFalse(sqlValidator.isValidSelect("SELECT * FROM users; --\n DROP TABLE users;"));
    }
}
