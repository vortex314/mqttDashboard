package be.limero.dashboard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    public AnchorPane anchorPane;
    public String connectionUrl = "tcp://test.mosquitto.org";
    public SimpleBooleanProperty mqttConnected=new SimpleBooleanProperty();


    private static final Logger log
            = LoggerFactory.getLogger(Controller.class);

    Mqtt mqtt = new Mqtt();
    HashMap<String, Observer<MqttMessage>> topicObserver = new HashMap<String, Observer<MqttMessage>>();

    void scanChildren(Pane parent) {
        ObservableList<Node> children = parent.getChildren();
        for (Node node : children) {
            if (node instanceof MqttProperty) {
                MqttProperty mqttProperty=(MqttProperty)node;
                mqttProperty.setMqtt(mqtt);
                mqtt.register(mqttProperty);
                log.info(" "+node.getClass()+" : "+mqttProperty.getSrc());
                if (node instanceof Initializable) ((Initializable) node).initialize(null, null);
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
        mqttConnected.bind(mqtt.mqttConnected);
        Long time = System.currentTimeMillis();
    }

}
