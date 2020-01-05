package be.limero.dashboard;

import eu.hansolo.medusa.Gauge;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import static be.limero.dashboard.Util.now;

public class SetText implements Subscriber<String> {
    Label node;
    Integer timeout = 2000;
    Long setTime;
    Timer timer = new Timer(true);

    private static final Logger log
            = LoggerFactory.getLogger(SetText.class);

    public SetText(Label node) {
        this.node = node;
        setTime = now();
        setTimeout(timeout);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
    }

    @Override
    public void onNext(String s) {
        setTime = now();
        Platform.runLater(() -> {
                    node.setStyle(null);
                    node.setDisable(false);
                    node.setText(s);
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
                    //                   node.setStyle("-fx-background-color: #FF0000;");
                    node.setDisable(true);
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
