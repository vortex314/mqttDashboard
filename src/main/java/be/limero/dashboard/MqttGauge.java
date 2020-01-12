package be.limero.dashboard;

import eu.hansolo.medusa.Gauge;
import javafx.fxml.Initializable;
import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.MqttTopic;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static be.limero.dashboard.Util.now;

public class MqttGauge extends Gauge implements MqttProperty<Double>, Initializable {
    @Setter @Getter String topic;
    @Setter @Getter Boolean retained;
    @Setter @Getter Integer qos;
    @Setter @Getter Integer disableTimeout=Integer.MAX_VALUE;
    @Setter @Getter Mqtt mqtt;

    Long setTime;
    Timer timer=new Timer(true);
    private static final Logger log
            = LoggerFactory.getLogger(MqttGauge.class);

    public MqttGauge() {
        super(SkinType.GAUGE);
        setTime=now();
    }

    @Override public String getUserAgentStylesheet() {
        return Gauge.class.getResource("gauge.css").toExternalForm();
    }


    Boolean timedout() {
        return  (now()-setTime) > disableTimeout;
    }

    @Override
    public void onSubscribe(Subscription subscription) {

    }

    @Override
    public void onNext(Double d) {
        setTime=now();
       setValue(d);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

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
