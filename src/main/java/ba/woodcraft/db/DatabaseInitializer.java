package ba.woodcraft.db;

import ba.woodcraft.dao.UserDAO;

import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(100) NOT NULL UNIQUE,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(20) NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS materials (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(120) NOT NULL,
                        cost_per_area DOUBLE NOT NULL,
                        cost_per_volume DOUBLE NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user_materials (
                        user_id INT NOT NULL,
                        material_id INT NOT NULL,
                        PRIMARY KEY (user_id, material_id),
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE
                    )
                    """);
        } catch (Exception e) {
            e.printStackTrace();
        }

        UserDAO userDAO = new UserDAO();
        if (!userDAO.hasAnyUsers()) {
            userDAO.createAdminSeed();
        }
    }
}
