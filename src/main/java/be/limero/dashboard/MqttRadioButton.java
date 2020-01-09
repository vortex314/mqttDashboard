package be.limero.dashboard;

import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;

import static be.limero.dashboard.Util.now;

public class MqttRadioButton extends RadioButton implements MqttProperty<Boolean>, Initializable {
    @Setter
    @Getter
    String topic;
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

    private static final Logger log
            = LoggerFactory.getLogger(MqttRadioButton.class);
    Timer timer = new Timer(true);
    Long lastUpdated = now();

    public MqttRadioButton() {
        super();
        lastUpdated = now();
        this.setOnAction((actionEvent) -> {
            mqtt.publish(topic, String.valueOf(this.isSelected()));
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info(" initialized topic : " + topic + " disableTimeout " + disableTimeout);
    }

    @Override
    public void onSubscribe(Subscription subscription) {

    }

    @Override
    public void onNext(Boolean aBoolean) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
