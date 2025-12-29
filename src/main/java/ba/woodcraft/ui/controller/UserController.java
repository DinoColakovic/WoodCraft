package ba.woodcraft.ui.controller;

import ba.woodcraft.dao.MaterialDAO;
import ba.woodcraft.model.Material;
import ba.woodcraft.util.CurrentUser;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class UserController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Material> materialTable;
    @FXML private TableColumn<Material, String> nameColumn;
    @FXML private TableColumn<Material, String> thicknessColumn;
    @FXML private TableColumn<Material, String> costPerAreaColumn;
    @FXML private TableColumn<Material, String> costPerVolumeColumn;
    @FXML private TextField nameField;
    @FXML private TextField thicknessField;
    @FXML private TextField costPerAreaField;
    @FXML private TextField costPerVolumeField;

    private final MaterialDAO materialDAO = new MaterialDAO();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        thicknessColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDouble(data.getValue().getThickness())));
        costPerAreaColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDouble(data.getValue().getCostPerArea())));
        costPerVolumeColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDouble(data.getValue().getCostPerVolume())));
        loadMaterials();
    }

    public void setWelcome(String username) {
        welcomeLabel.setText("Dobrodošli, " + username + " (USER)");
    }

    @FXML
    public void onAddMaterial(ActionEvent event) {
        Integer userId = CurrentUser.getId();
        if (userId == null) {
            info("Greška", "Korisnik nije prijavljen.");
            return;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            info("Greška", "Naziv materijala je obavezan.");
            return;
        }
        Double thickness = parseDouble(thicknessField.getText(), "Debljina");
        Double costPerArea = parseDouble(costPerAreaField.getText(), "Cijena po površini");
        Double costPerVolume = parseDouble(costPerVolumeField.getText(), "Cijena po volumenu");
        if (thickness == null || costPerArea == null || costPerVolume == null) {
            return;
        }
        Material created = materialDAO.addMaterialForUser(userId,
                new Material(0, name, thickness, costPerArea, costPerVolume));
        if (created == null) {
            info("Greška", "Materijal nije moguće dodati.");
            return;
        }
        materialTable.getItems().add(created);
        clearMaterialForm();
    }

    @FXML
    public void onRemoveMaterial(ActionEvent event) {
        Integer userId = CurrentUser.getId();
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (userId == null || selected == null) {
            info("Greška", "Odaberite materijal za uklanjanje.");
            return;
        }
        if (!materialDAO.removeUserMaterial(userId, selected.getId())) {
            info("Greška", "Materijal nije moguće ukloniti.");
            return;
        }
        materialTable.getItems().remove(selected);
    }

    @FXML
    public void onLogout(ActionEvent event) {
        CurrentUser.clear();
        SceneNavigator.show("view/login.fxml");
    }

    @FXML
    public void onOpenCanvas(ActionEvent event) {
        SceneNavigator.show("view/canvas.fxml");
    }

    private void loadMaterials() {
        Integer userId = CurrentUser.getId();
        if (userId == null) {
            return;
        }
        materialTable.getItems().setAll(materialDAO.findMaterialsForUser(userId));
    }

    private void clearMaterialForm() {
        nameField.clear();
        thicknessField.clear();
        costPerAreaField.clear();
        costPerVolumeField.clear();
    }

    private Double parseDouble(String value, String label) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            info("Greška", label + " mora biti broj.");
            return null;
        }
    }

    private String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    private void info(String naslov, String poruka) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(naslov);
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
