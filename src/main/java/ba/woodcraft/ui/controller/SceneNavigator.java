package ba.woodcraft.ui.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {

    private static Stage stage;

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void show(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(
                    SceneNavigator.class.getClassLoader().getResource(fxmlPath)
            );
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Ne mogu učitati FXML: " + fxmlPath, e);
        }
    }

    public static <T> T showWithController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getClassLoader().getResource(fxmlPath)
            );
            Parent root = loader.load();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
            return loader.getController();
        } catch (Exception e) {
            throw new RuntimeException("Ne mogu učitati FXML: " + fxmlPath, e);
        }
    }
}
