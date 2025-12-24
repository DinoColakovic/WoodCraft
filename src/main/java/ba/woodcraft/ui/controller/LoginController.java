package ba.woodcraft.ui.controller;

import ba.woodcraft.dao.UserDAO;
import ba.woodcraft.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ba.woodcraft.ui.controller.AdminController;
import ba.woodcraft.ui.controller.UserController;
import ba.woodcraft.ui.controller.SceneNavigator;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            prikazi("Greška", "Unesi username i password.");
            return;
        }

        UserDAO.DbUser user = userDAO.findByUsername(username);

        if (user == null) {
            prikazi("Greška", "Korisnik ne postoji.");
            return;
        }

        if (!PasswordUtil.provjeriLozinku(password, user.passwordHash())) {
            prikazi("Greška", "Pogrešna lozinka.");
            return;
        }

        // ✅ Redirect po ulozi
        String role = user.role();

        if ("ADMIN".equalsIgnoreCase(role)) {
            AdminController c = SceneNavigator.showWithController("view/admin.fxml");
            c.setWelcome(user.username());
        } else {
            UserController c = SceneNavigator.showWithController("view/user.fxml");
            c.setWelcome(user.username());
        }
    }

    @FXML
    public void onOpenRegister(ActionEvent event) {
        SceneNavigator.show("view/register.fxml");
    }

    private void prikazi(String naslov, String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
