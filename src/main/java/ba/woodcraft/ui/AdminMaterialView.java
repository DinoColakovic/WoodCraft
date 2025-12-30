package ba.woodcraft.ui;

import ba.woodcraft.dao.MaterialDAO;
import ba.woodcraft.dao.UserDAO;
import ba.woodcraft.model.Material;
import ba.woodcraft.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminMaterialView {
    private final VBox root = new VBox(12);
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final UserDAO userDAO = new UserDAO();

    private final ComboBox<User> userCombo = new ComboBox<>();
    private final ComboBox<Material> materialCombo = new ComboBox<>();
    private final TextField nameField = new TextField();
    private final TextField costAreaField = new TextField();
    private final TextField costVolumeField = new TextField();

    public AdminMaterialView() {
        root.setPadding(new Insets(15));
        root.getChildren().addAll(
                buildMaterialCreator(),
                buildAssignmentPanel()
        );
        refreshData();
    }

    public VBox getRoot() {
        return root;
    }

    private VBox buildMaterialCreator() {
        VBox box = new VBox(10);
        Label title = new Label("Create Material");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        nameField.setPromptText("Material name");
        costAreaField.setPromptText("Cost per area");
        costVolumeField.setPromptText("Cost per volume");

        Button create = new Button("Create Material");
        create.setOnAction(event -> {
            Double costArea = parseDouble(costAreaField.getText());
            Double costVolume = parseDouble(costVolumeField.getText());
            if (nameField.getText().trim().isEmpty() || costArea == null || costVolume == null) {
                alert("Fill in name and numeric costs.");
                return;
            }
            Material created = materialDAO.createMaterial(nameField.getText().trim(), costArea, costVolume);
            if (created == null) {
                alert("Could not create material.");
                return;
            }
            nameField.clear();
            costAreaField.clear();
            costVolumeField.clear();
            refreshMaterials();
        });

        box.getChildren().addAll(title, nameField, costAreaField, costVolumeField, create);
        return box;
    }

    private VBox buildAssignmentPanel() {
        VBox box = new VBox(10);
        Label title = new Label("Assign Materials to Users");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        userCombo.setPromptText("Select user");
        materialCombo.setPromptText("Select material");

        Button assign = new Button("Assign Material");
        assign.setOnAction(event -> {
            User user = userCombo.getValue();
            Material material = materialCombo.getValue();
            if (user == null || material == null) {
                alert("Pick a user and material.");
                return;
            }
            boolean ok = materialDAO.assignMaterialToUser(user.getId(), material.getId());
            if (ok) {
                alert("Assigned material to user.");
            } else {
                alert("Assignment failed.");
            }
        });

        HBox row = new HBox(10, userCombo, materialCombo, assign);
        box.getChildren().addAll(title, row);
        return box;
    }

    private void refreshData() {
        refreshUsers();
        refreshMaterials();
    }

    private void refreshUsers() {
        List<User> users = userDAO.listUsers();
        userCombo.getItems().setAll(users);
    }

    private void refreshMaterials() {
        List<Material> materials = materialDAO.listAllMaterials();
        materialCombo.getItems().setAll(materials);
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
