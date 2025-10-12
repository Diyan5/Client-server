package registrationApp.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Базов тестов хелпър: регистрира фалшив MySQL драйвер към H2 (MySQL режим),
 * създава таблицата users и я изчиства след всеки тест.
 */
public abstract class TestDbHarness {

    // ---- вътрешен драйвер: прихваща "jdbc:mysql:" и пренасочва към H2 ----
    public static final class FakeMySqlDriver implements Driver {
        private static final org.h2.Driver DELEGATE = new org.h2.Driver();
        private static final String H2_URL =
                "jdbc:h2:mem:registration_app;MODE=MySQL;DB_CLOSE_DELAY=-1;" +
                        "DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            if (!acceptsURL(url)) return null;
            return DELEGATE.connect(H2_URL, new Properties());
        }

        @Override public boolean acceptsURL(String url) { return url != null && url.startsWith("jdbc:mysql:"); }
        @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
        @Override public int getMajorVersion() { return DELEGATE.getMajorVersion(); }
        @Override public int getMinorVersion() { return DELEGATE.getMinorVersion(); }
        @Override public boolean jdbcCompliant() { return DELEGATE.jdbcCompliant(); }
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            // Няма parent logger – стандартно поведение е да хвърлим, както позволяват JDBC драйверите
            throw new SQLFeatureNotSupportedException("getParentLogger not supported");
        }
    }

    @BeforeAll
    static void initSchema() throws SQLException {
        // Регистрираме вътрешния драйвер преди първата конекция
        DriverManager.registerDriver(new FakeMySqlDriver());

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
    void cleanTables() throws SQLException {
        try (Connection c = Db.get(); Statement st = c.createStatement()) {
            st.execute("DELETE FROM users");
            try {
                st.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1"); // H2 синтаксис
            } catch (SQLException e) {
                st.execute("ALTER TABLE users AUTO_INCREMENT = 1"); // MySQL fallback
            }
        }
    }
}
