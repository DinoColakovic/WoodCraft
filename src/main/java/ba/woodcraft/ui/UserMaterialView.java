package ba.woodcraft.ui;

import ba.woodcraft.dao.MaterialDAO;
import ba.woodcraft.model.Material;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class UserMaterialView {
    private final VBox root = new VBox(12);
    private final MaterialDAO materialDAO = new MaterialDAO();

    private final TextField nameField = new TextField();
    private final TextField costAreaField = new TextField();
    private final TextField costVolumeField = new TextField();
    private final ListView<Material> materialList = new ListView<>();

    public UserMaterialView() {
        root.setPadding(new Insets(15));
        root.getChildren().addAll(buildCreator(), buildList());
        refreshMaterials();
    }

    public VBox getRoot() {
        return root;
    }

    private VBox buildCreator() {
        VBox box = new VBox(8);
        Label title = new Label("Your Materials");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        nameField.setPromptText("Material name");
        costAreaField.setPromptText("Cost per area");
        costVolumeField.setPromptText("Cost per volume");

        Button create = new Button("Add Material");
        create.setOnAction(event -> handleCreate());

        box.getChildren().addAll(title, nameField, costAreaField, costVolumeField, create);
        return box;
    }

    private VBox buildList() {
        VBox box = new VBox(8);
        materialList.setPrefHeight(220);

        Button remove = new Button("Remove Selected");
        remove.setOnAction(event -> handleRemove());

        HBox actions = new HBox(10, remove);
        box.getChildren().addAll(new Label("Assigned materials"), materialList, actions);
        return box;
    }

    private void handleCreate() {
        if (Session.getUser() == null) {
            alert("Log in first.");
            return;
        }
        String name = nameField.getText().trim();
        Double costArea = parseDouble(costAreaField.getText());
        Double costVolume = parseDouble(costVolumeField.getText());
        if (name.isEmpty() || costArea == null || costVolume == null) {
            alert("Fill in name and numeric costs.");
            return;
        }
        Material created = materialDAO.createMaterial(name, costArea, costVolume);
        if (created == null) {
            alert("Material could not be created.");
            return;
        }
        materialDAO.assignMaterialToUser(Session.getUser().getId(), created.getId());
        nameField.clear();
        costAreaField.clear();
        costVolumeField.clear();
        refreshMaterials();
    }

    private void handleRemove() {
        if (Session.getUser() == null) {
            alert("Log in first.");
            return;
        }
        Material selected = materialList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Select a material to remove.");
            return;
        }
        boolean removed = materialDAO.removeMaterialForUser(Session.getUser().getId(), selected.getId());
        if (!removed) {
            alert("Could not remove material.");
            return;
        }
        refreshMaterials();
    }

    private void refreshMaterials() {
        if (Session.getUser() == null) {
            return;
        }
        List<Material> materials = materialDAO.listMaterialsForUser(Session.getUser().getId());
        materialList.getItems().setAll(materials);
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
