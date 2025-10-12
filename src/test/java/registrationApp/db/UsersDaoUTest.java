package registrationApp.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import registrationApp.model.User;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UsersDaoUTest extends TestDbHarness {

    private UsersDao dao;

    @BeforeEach
    void setUp() { dao = new UsersDao(); }

    @Test
    void findIdByEmail_returnsEmpty_whenUserMissing() throws SQLException {
        assertTrue(dao.findIdByEmail("missing@example.com").isEmpty());
    }

    @Test
    void insert_returnsGeneratedId_andFindersWork() throws SQLException {
        byte[] hash = new byte[]{1,2,3};
        byte[] salt = new byte[]{4,5};

        long id = dao.insert("Alice", "alice@example.com", hash, salt);
        assertTrue(id > 0);

        Optional<Long> foundId = dao.findIdByEmail("alice@example.com");
        assertTrue(foundId.isPresent());
        assertEquals(id, foundId.get());

        Optional<User> userOpt = dao.findByEmail("alice@example.com");
        assertTrue(userOpt.isPresent());

        User u = userOpt.get();
        assertEquals(id, u.id());
        assertEquals("Alice", u.name());
        assertEquals("alice@example.com", u.email());
        assertArrayEquals(hash, u.passHash());
        assertArrayEquals(salt, u.passSalt());
    }

    @Test
    void findByEmail_returnsEmpty_whenUserMissing() throws SQLException {
        assertTrue(dao.findByEmail("nobody@nowhere.com").isEmpty());
    }

    @Test
    void insert_multipleRows_haveIncreasingIds_andBothRetrievable() throws SQLException {
        long id1 = dao.insert("John", "john@ex.com", new byte[]{9}, new byte[]{8});
        long id2 = dao.insert("Jane", "jane@ex.com", new byte[]{7,7}, new byte[]{6,6});
        assertEquals(1L, id1);
        assertEquals(2L, id2);

        assertEquals(Optional.of(1L), dao.findIdByEmail("john@ex.com"));
        assertEquals(Optional.of(2L), dao.findIdByEmail("jane@ex.com"));
    }

    @Test
    void insert_throwsSQLException_onDuplicateEmail() throws SQLException {
        dao.insert("Dup", "dup@ex.com", new byte[]{1}, new byte[]{2});
        assertThrows(SQLException.class, () ->
                dao.insert("Dup2", "dup@ex.com", new byte[]{3}, new byte[]{4})
        );
    }
}
