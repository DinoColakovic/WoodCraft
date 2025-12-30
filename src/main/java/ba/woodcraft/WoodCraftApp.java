package ba.woodcraft;

import ba.woodcraft.db.DatabaseInitializer;
import ba.woodcraft.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WoodCraftApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseInitializer.initialize();
        LoginView loginView = new LoginView(primaryStage);
        Scene scene = new Scene(loginView.getRoot(), 420, 320);
        primaryStage.setTitle("WoodCraft");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
