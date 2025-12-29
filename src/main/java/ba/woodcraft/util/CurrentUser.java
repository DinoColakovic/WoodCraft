package ba.woodcraft.util;

import ba.woodcraft.dao.UserDAO;

public final class CurrentUser {
    private static Integer id;
    private static String username;
    private static String role;

    private CurrentUser() {
    }

    public static void set(UserDAO.DbUser user) {
        if (user == null) {
            clear();
            return;
        }
        id = user.id();
        username = user.username();
        role = user.role();
    }

    public static void clear() {
        id = null;
        username = null;
        role = null;
    }

    public static Integer getId() {
        return id;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }
}
