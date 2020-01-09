package sample;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import static be.limero.dashboard.Util.now;


public class MqttBooleanLabel extends Label implements MqttTopic, Subscriber<Boolean> {
    String topic = "NO_TOPIC";
    Integer timeout = Integer.MAX_VALUE;
    Long setTime;
    Timer timer = new Timer(true);
    private static final Logger log
            = LoggerFactory.getLogger(MqttBooleanLabel.class);

    public MqttBooleanLabel() {
        super();//don't forget our our parents!
        setTime = now();
    }

    Boolean timedout() {
        return (now() - setTime) > timeout;
    }

    @Override
    public void onNext(Boolean bool) {
        setTime = now();
        Platform.runLater(() -> {
                    if (bool) setStyle("-fx-background-color: #00FF00;");
                    else setStyle("-fx-background-color: #FF0000;");
                }
        );
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onSubscribe(Subscription subscription) {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if ((now() - setTime) > timeout) {
                    log.info(getTopic() + " delta : " + ((now() - setTime) + " > " + timeout));
                    onNext(false);
                }
            }
        }, 0, timeout);
    }

}