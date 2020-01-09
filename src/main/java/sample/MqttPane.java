package sample;

import eu.hansolo.medusa.Gauge;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static be.limero.dashboard.Util.now;

public class MqttPane extends FlowPane implements Initializable, Observer<MqttMessage> {
    String topic;
    Integer disableTimeout = Integer.MAX_VALUE;
    Integer sampleCount = 10;
    Integer sampleTimeout = 60;
    Timer timer = new Timer(true);
    Node node;
    Long setTime;
    private static final Logger log
            = LoggerFactory.getLogger(MqttPane.class);

    public MqttPane() {
        super();
        this.node = node;
        setTime = now();
        setDisableTimeout(disableTimeout);
    }


    void forAllChildren(Pane parent, ProcessNode pn) {
        for (Node node : parent.getChildren()) {
            if (node instanceof Pane) {
                forAllChildren((Pane) node, pn);
            } else {
                pn.process(node);
            }
        }
    }

    static void setValue(Node n, Object o) {
        Platform.runLater(() -> {
            if (n instanceof Gauge && o instanceof Double) ((Gauge) n).setValue((Double) o);
            if (n instanceof Gauge && o instanceof Integer) ((Gauge) n).setValue((Integer) o * 1.0);
            if (n instanceof Label && o instanceof Boolean) {
                if ((Boolean) o) n.setStyle("-fx-background-color: #00FF00;");
                else n.setStyle("-fx-background-color: #FF0000;");
            }
        });
    }

    void setChildren(Pane parent, Object value) {
        forAllChildren(parent, node -> setValue(node, value));
        forAllChildren(parent, node -> node.setDisable(false));
    }

    void disableChildren(Pane parent, Boolean disable) {
        forAllChildren(parent, node -> node.setDisable(true));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getDisableTimeout() {
        return disableTimeout;
    }

    public void setDisableTimeout(Integer disableTimeout) {
        Pane thisPane = this;
        this.disableTimeout = disableTimeout;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if ((now() - setTime) > disableTimeout) {
                    log.info(" delta : " + ((now() - setTime) + " > " + disableTimeout));
                    disableChildren(thisPane, true);
                }
            }
        }, disableTimeout, disableTimeout);
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Integer getSampleTimeout() {
        return sampleTimeout;
    }

    public void setSampleTimeout(Integer sampleTimeout) {
        this.sampleTimeout = sampleTimeout;
    }

    @Override
    public void onCompleted() {
        log.warn("onCompleted");
    }

    @Override
    public void onError(Throwable throwable) {
        log.warn("onError", throwable);
    }

    @Override
    public void onNext(MqttMessage mqttMessage) {
        String message = new String(mqttMessage.getPayload());
        Object json = new JSONTokener(message).nextValue();
        setChildren(this, json);
    }
}
