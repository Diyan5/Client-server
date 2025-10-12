package registrationApp.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DbUTest extends TestDbHarness {

    @Test
    void get_returnsValidConnection_andCanExecuteSimpleQuery() throws SQLException {
        try (Connection c = Db.get()) {
            assertNotNull(c);
            assertTrue(c.isValid(2), "Connection should be valid");

            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT 1")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
                assertFalse(rs.next());
            }
        }
    }
}
