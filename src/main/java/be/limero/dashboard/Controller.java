package be.limero.dashboard;

import eu.hansolo.medusa.Gauge;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONTokener;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    public AnchorPane anchorPane;
    public String url = "tcp://test.mosquitto.org";


    ValuePublisher<Boolean> mqttIsConnected = new ValuePublisher<Boolean>();

    private static final Logger log
            = LoggerFactory.getLogger(Controller.class);

    Mqtt mqtt=new Mqtt();
    HashMap<String, Observer<MqttMessage>> topicObserver = new HashMap<String, Observer<MqttMessage>>();

    void scanChildren(Pane parent) {
        ObservableList<Node> children = parent.getChildren();
        for (Node node : children) {
            if (node instanceof MqttProperty) {
                ((MqttProperty) node).setMqtt(mqtt);
                if ( node instanceof Initializable ) ((Initializable)node).initialize(null,null);
            } else if (node instanceof Pane) {
                scanChildren((Pane) node);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Controller started " + url + "  " + resourceBundle);
        scanChildren(anchorPane);
        mqtt.connect();
        Long time = System.currentTimeMillis();
    }


    void mqttConnect() {
        try {
            log.info("mqtt connecting...");
            mqtt.connect();
            mqtt.subscribe("src/#", 0);
            log.info("mqtt connected.");
            mqttIsConnected.set(true);
        } catch (Exception ex) {
            log.warn("failed ", ex);
            mqttIsConnected.set(false);
        }
    }

}
