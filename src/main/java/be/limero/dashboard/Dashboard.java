package be.limero.dashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Dashboard extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MqttDashboard.fxml"));
        primaryStage.setTitle("MQTT Dashboard");
        primaryStage.setScene(new Scene(root, 700, 300));
        primaryStage.show();
    }
}
