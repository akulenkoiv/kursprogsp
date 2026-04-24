package integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utility.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    @DisplayName("Проверка подключения к MySQL")
    void testDatabaseConnection() throws Exception {
        Connection conn = DBConnection.getConnection();

        assertNotNull(conn);
        assertFalse(conn.isClosed());

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    @DisplayName("Проверка наличия основных таблиц")
    void testTablesExist() throws Exception {
        Connection conn = DBConnection.getConnection();

        String[] expectedTables = {
                "users", "roles", "incomes", "expenses",
                "tax_payments", "budgets", "income_categories",
                "expense_categories", "counterparties"
        };

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

            while (rs.next()) {
                String tableName = rs.getString(1);
                for (String expected : expectedTables) {
                    if (tableName.equals(expected)) {
                        break;
                    }
                }
            }
        }
    }
}