package ba.woodcraft.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL =
            System.getProperty("woodcraft.db.url",
                    "jdbc:mysql://localhost:3306/woodcraft?useSSL=false&serverTimezone=UTC");
    private static final String USER = System.getProperty("woodcraft.db.user", "root");
    private static final String PASSWORD = System.getProperty("woodcraft.db.password", "root");

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}
