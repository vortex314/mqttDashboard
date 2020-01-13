package be.limero.dashboard;


import eu.hansolo.medusa.Gauge;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static be.limero.dashboard.Util.now;

public class MqttLabel extends Label implements MqttProperty<Object>, Initializable {
    @Setter @Getter String topic;
    @Setter @Getter Boolean retained;
    @Setter @Getter Integer qos;
    @Setter @Getter Integer disableTimeout=2000;
    @Setter @Getter Mqtt mqtt;

    Long setTime;
    Timer timer=new Timer(true);
    private static final Logger log
            = LoggerFactory.getLogger(MqttLabel.class);

    public MqttLabel() {
        super();
        setTime=now();
        setText("waiting...");
    }

    @Override public String getUserAgentStylesheet() {
        return Gauge.class.getResource("gauge.css").toExternalForm();
    }


    Boolean timedout() {
        return  (now()-setTime) > disableTimeout;
    }

    @Override
    public void onNext(Object d) {
        setTime=now();
        Platform.runLater(()->setText(d.toString()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if ( (timedout())) {
                    if (timedout()) setDisable(true);
                    else setDisable(false);
                }
            }
        }, disableTimeout, disableTimeout);
    }
}
