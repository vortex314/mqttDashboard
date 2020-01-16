package be.limero.dashboard;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.ResourceBundle;

public class MqttButton extends Button implements MqttProperty<Boolean>, Initializable {
    @Setter
    @Getter
    String src="src/unknown";
    @Setter
    @Getter
    String dst="dst/unknown";
    @Setter
    @Getter
    Boolean retained;
    @Setter
    @Getter
    Integer qos;
    @Setter
    @Getter
    Integer disableTimeout = Integer.MAX_VALUE;
    @Setter
    @Getter
    Mqtt mqtt;

    @Override
    public void onNext(Object value) {
        if (value instanceof String) {
            Platform.runLater(()->{
            setText((String) value);});
        } else if (value instanceof Boolean) {
            setDisable(!(Boolean) value);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mqtt.publish(dst,true);
            }
        });
    }
}
