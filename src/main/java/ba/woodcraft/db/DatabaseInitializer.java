package ba.woodcraft.db;

import ba.woodcraft.dao.UserDAO;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(DBConnection.getUrl(), DBConnection.getUser(), DBConnection.getPassword())
                    .load();
            flyway.migrate();
        } catch (Exception e) {
            logger.error("Database migration failed.", e);
        }

        UserDAO userDAO = new UserDAO();
        if (!userDAO.hasAnyUsers()) {
            userDAO.createAdminSeed();
        }
    }
}
