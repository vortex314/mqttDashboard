package be.limero.dashboard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Dashboard extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("LawnMower.fxml"));
        primaryStage.setTitle("MQTT Dashboard");

        primaryStage.setResizable(true);
        primaryStage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                Platform.setImplicitExit(true);
                primaryStage.close();
            }
        });
        primaryStage.show();
    }
}
