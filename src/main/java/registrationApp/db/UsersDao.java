package registrationApp.db;

import registrationApp.model.User;

import java.sql.*;
import java.util.Optional;

public final class UsersDao {
    public Optional<Long> findIdByEmail(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(rs.getLong(1));
            }
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, pass_hash, pass_salt FROM users WHERE email = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new User(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getBytes("pass_hash"),
                        rs.getBytes("pass_salt")
                ));
            }
        }
    }

    public long insert(String name, String email, byte[] passHash, byte[] passSalt) throws SQLException {
        String sql = "INSERT INTO users(name, email, pass_hash, pass_salt) VALUES (?,?,?,?)";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setBytes(3, passHash);
            ps.setBytes(4, passSalt);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }
}
