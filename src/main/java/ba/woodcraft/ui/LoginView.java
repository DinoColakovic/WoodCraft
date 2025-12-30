package ba.woodcraft.ui;

import ba.woodcraft.dao.UserDAO;
import ba.woodcraft.model.Role;
import ba.woodcraft.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {
    private final VBox root = new VBox(12);
    private final UserDAO userDAO = new UserDAO();

    public LoginView(Stage stage) {
        Label title = new Label("WoodCraft Login");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label message = new Label();
        message.setStyle("-fx-text-fill: #b00020;");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register as User");

        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                message.setText("Enter username and password.");
                return;
            }
            User user = userDAO.login(username, password);
            if (user == null) {
                message.setText("Invalid credentials.");
                return;
            }
            Session.setUser(user);
            MainView mainView = new MainView(stage);
            stage.setScene(new Scene(mainView.getRoot(), 1200, 800));
        });

        registerButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                message.setText("Choose a username and password.");
                return;
            }
            boolean created = userDAO.createUser(username, password, Role.USER);
            if (!created) {
                message.setText("Username already exists.");
                return;
            }
            message.setStyle("-fx-text-fill: #2e7d32;");
            message.setText("Account created. You can log in.");
        });

        HBox buttons = new HBox(10, loginButton, registerButton);
        buttons.setAlignment(Pos.CENTER);

        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(title, usernameField, passwordField, buttons, message);
    }

    public Parent getRoot() {
        return root;
    }
}
