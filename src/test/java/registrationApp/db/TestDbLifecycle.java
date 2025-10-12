package registrationApp.db;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestDbLifecycle {

    @BeforeAll
    void _initDbSchema() throws SQLException {
        // Регистрирай драйвера (както в твоя TestDbHarness)
        DriverManager.registerDriver(new TestDbHarness.FakeMySqlDriver());

        try (Connection c = Db.get(); Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(255) NOT NULL,
                  email VARCHAR(255) NOT NULL UNIQUE,
                  pass_hash VARBINARY(255) NOT NULL,
                  pass_salt VARBINARY(255) NOT NULL
                );
            """);
        }
    }

    @AfterEach
    void _cleanUsers() throws SQLException {
        try (Connection c = Db.get(); Statement st = c.createStatement()) {
            st.execute("DELETE FROM users");
            try {
                st.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
            } catch (SQLException e) {
                st.execute("ALTER TABLE users AUTO_INCREMENT = 1");
            }
        }
    }
}
