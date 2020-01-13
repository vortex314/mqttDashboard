package be.limero.dashboard;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ChoiceBox;
import lombok.Getter;
import lombok.Setter;

public class MqttChoiceBox extends ChoiceBox<String> implements MqttProperty<String> {
    @Setter
    @Getter
    String topic;
    @Setter @Getter Boolean retained;
    @Setter @Getter Integer qos;
    @Setter @Getter Integer disableTimeout=2000;
    @Setter @Getter Mqtt mqtt;
    public MqttChoiceBox(){
        super();
        this.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                mqtt.publish(topic,newValue);
            }
        });
    }

    @Override
    public void onNext(Object value) {

    }

}
