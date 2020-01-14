package be.limero.dashboard;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.ResourceBundle;

public class MqttButton extends Button implements MqttProperty<Boolean>, Initializable {
    @Setter
    @Getter
    String src;
    @Setter
    @Getter
    String dst;
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

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
