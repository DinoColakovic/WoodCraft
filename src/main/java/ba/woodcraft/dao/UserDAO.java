package ba.woodcraft.dao;

import ba.woodcraft.db.DBConnection;
import ba.woodcraft.model.Role;
import ba.woodcraft.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public boolean hasAnyUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createAdminSeed() {
        createUser("admin", "admin", Role.ADMIN);
    }

    public User login(String username, String password) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                return null;
            }
            String hash = rs.getString("password_hash");
            if (!BCrypt.checkpw(password, hash)) {
                return null;
            }
            Role role = Role.valueOf(rs.getString("role"));
            return new User(rs.getInt("id"), rs.getString("username"), role);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean createUser(String username, String password, Role role) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            statement.setString(3, role.name());
            return statement.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> listUsers() {
        String sql = "SELECT id, username, role FROM users ORDER BY username";
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        Role.valueOf(rs.getString("role"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }
}
