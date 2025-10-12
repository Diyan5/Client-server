package registrationApp.db;

import org.h2.Driver;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Прихваща "jdbc:mysql:" URL и го пренасочва към H2 in-memory в MySQL режим.
 */
public final class FakeMySqlDriver extends Driver {
    private static final Driver DELEGATE = new Driver();
    // Единно in-memory DB име за всички конекции в тестовете
    private static final String H2_URL =
            "jdbc:h2:mem:registration_app;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";

    static {
        try {
            DriverManager.registerDriver(new FakeMySqlDriver());
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) return null;
        // Игнорираме потребител/парола – H2 in-memory не ги изисква
        return DELEGATE.connect(H2_URL, new Properties());
    }

    @Override public boolean acceptsURL(String url) { return url != null && url.startsWith("jdbc:mysql:"); }
    @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
    @Override public int getMajorVersion() { return DELEGATE.getMajorVersion(); }
    @Override public int getMinorVersion() { return DELEGATE.getMinorVersion(); }
    @Override public boolean jdbcCompliant() { return DELEGATE.jdbcCompliant(); }
    @Override public Logger getParentLogger() {
        return DELEGATE.getParentLogger();
    }
}
