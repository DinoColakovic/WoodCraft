package ba.woodcraft.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role;

    public User(int id, String username, String passwordHash, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // getters & setters
}