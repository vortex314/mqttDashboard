package be.limero.dashboard;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MqttChoiceBox extends ChoiceBox<String> implements MqttProperty<String>, Initializable {
    @Setter
    @Getter
    String src = "src/unknown", dst = "dst/unknown";
    @Setter
    @Getter
    Boolean retained;
    @Setter
    @Getter
    Integer qos;
    @Setter
    @Getter
    Integer disableTimeout = 2000;
    @Setter
    @Getter
    Mqtt mqtt;

    private static final Logger log
            = LoggerFactory.getLogger(MqttChoiceBox.class);

    public MqttChoiceBox() {
        super();

    }

    @Override
    public void onNext(Object obj) {
        {
            if (obj instanceof String) {
                String s = (String) obj;
                Platform.runLater(() -> {
                    this.setValue(s);
                });
            } else {
                log.warn("expect String for "+this.getClass()+" received "+obj.getClass());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Platform.runLater(() -> {
                    mqtt.publish(dst, newValue);
                });
            }
        });
    }
}
