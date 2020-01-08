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


public class Controller implements Initializable, MqttCallback {
    public AnchorPane anchorPane;
    public Gauge motorRpmTarget;
    public Gauge motorRpmMeasured;
    public Gauge motorCurrent;
    public Label mqttConnected;
    public Label motorRunning;
    public Label motorAlive;
    public Label motorMessage;
    public Slider rpmTargetSlider;
    public Gauge servoAngleTarget;
    public Gauge servoAngleMeasured;
    public Gauge servoCurrent;
    public Label servoRunning;
    public String url="tcp://test.mosquitto.org";


    ValuePublisher<Boolean> mqttIsConnected = new ValuePublisher<Boolean>();

    private static final Logger log
            = LoggerFactory.getLogger(Controller.class);

    MqttClient mqttClient;
    MqttConnectOptions mqttConnectOptions;
    HashMap<String, Observer<MqttMessage>> topicObserver = new HashMap<String, Observer<MqttMessage>>();

    void scanChildren(Pane parent) {
        ObservableList<Node> children = parent.getChildren();
        for (Node node : children) {
            if (node instanceof Pane) {
                if (node instanceof MqttPane) {
                    MqttPane mqttPane = (MqttPane) node;
                    topicObserver.put(mqttPane.getTopic(), mqttPane);
                } else
                    scanChildren((Pane) node);
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Controller started " + url + "  " + resourceBundle);
        scanChildren(anchorPane);
        mqttConnect();
        Long time = System.currentTimeMillis();

    }


    void mqttConnect() {
        try {
            log.info("mqtt connecting...");
     /*       String tmpDir = System.getProperty("java.io.tmpdir");
            MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);*/
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttClient = new MqttClient("tcp://limero.ddns.net:1883", "Paho" + System.nanoTime());
            mqttClient.setCallback(this);
            mqttClient.connect();
            mqttClient.subscribe("src/#", 0);
            log.info("mqtt connected.");
            mqttIsConnected.set(true);
        } catch (Exception ex) {
            log.warn("failed ", ex);
            mqttIsConnected.set(false);
        }
    }

    public void connect(ActionEvent actionEvent) {
        mqttConnect();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        log.warn("MQTT connection lost ");
    }


    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        Observer<MqttMessage> observer = topicObserver.get(topic);
        if (observer != null)
            observer.onNext(mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        Slider sl = (Slider) mouseEvent.getSource();
        Double d = sl.getValue();
        Integer i = d.intValue();
        try {
            String topic = "dst/drive/motor/rpmTarget";
            mqttClient.publish(topic, new MqttMessage(i.toString().getBytes()));
            log.info("MQTT TXD " + topic + ":" + i);
        } catch (Exception ex) {
            log.warn("MQTT publish failed ");
        }
    }
}
