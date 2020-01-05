package be.limero.dashboard;

import javafx.application.Platform;
import javafx.scene.Node;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.MqttBooleanLabel;

import java.util.Timer;
import java.util.TimerTask;

import static be.limero.dashboard.Util.now;

public class RedGreen implements Subscriber<Boolean> {
    Node node;
    Integer timeout = 2000;
    Long setTime;
    Timer timer = new Timer(true);
    private static final Logger log
            = LoggerFactory.getLogger(RedGreen.class);

    public RedGreen(Node node) {
        this.node = node;
        setTime = now();
        setTimeout(timeout);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
    }

    @Override
    public void onNext(Boolean bool) {
        setTime = now();
        Platform.runLater(() -> {
                    if (bool) node.setStyle("-fx-background-color: #00FF00;");
                    else node.setStyle("-fx-background-color: #FF0000;");
                }
        );
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
                    log.info(" delta : " + ((now() - setTime) + " > " + timeout));
                    onNext(false);
                }
            }
        }, timeout, timeout);
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
    }
}
