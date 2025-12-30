package ba.woodcraft.ui;

import ba.woodcraft.model.Role;
import ba.woodcraft.ui.canvas.CanvasView;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainView {
    private final BorderPane root = new BorderPane();

    public MainView(Stage stage) {
        CanvasView canvasView = new CanvasView();

        TabPane tabs = new TabPane();
        Tab canvasTab = new Tab("Canvas", canvasView.getRoot());
        canvasTab.setClosable(false);
        tabs.getTabs().add(canvasTab);

        UserMaterialView userMaterialView = new UserMaterialView();
        Tab materialsTab = new Tab("Materials", userMaterialView.getRoot());
        materialsTab.setClosable(false);
        tabs.getTabs().add(materialsTab);

        if (Session.getUser() != null && Session.getUser().getRole() == Role.ADMIN) {
            AdminMaterialView adminMaterialView = new AdminMaterialView();
            Tab adminTab = new Tab("Materials (Admin)", adminMaterialView.getRoot());
            adminTab.setClosable(false);
            tabs.getTabs().add(adminTab);
        }

        root.setTop(buildHeader(stage));
        root.setCenter(tabs);
    }

    public Parent getRoot() {
        return root;
    }

    private HBox buildHeader(Stage stage) {
        Label label = new Label();
        if (Session.getUser() != null) {
            label.setText("Logged in as: " + Session.getUser().getUsername() + " (" + Session.getUser().getRole() + ")");
        }
        Button logout = new Button("Logout");
        logout.setOnAction(event -> {
            Session.clear();
            LoginView loginView = new LoginView(stage);
            stage.setScene(new Scene(loginView.getRoot(), 420, 320));
        });

        HBox box = new HBox(12, label, logout);
        box.setPadding(new Insets(10));
        return box;
    }
}
