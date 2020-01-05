package be.limero.dashboard;

import eu.hansolo.medusa.Gauge;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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



    ValuePublisher<Boolean> mqttIsConnected=new ValuePublisher<Boolean>();

    private static final Logger log
            = LoggerFactory.getLogger(Controller.class);

    MqttClient mqttClient;
    MqttConnectOptions mqttConnectOptions;
    HashMap<String, Subscriber<?>> topicSubscriber = new HashMap<String, Subscriber<?>>();

    void scanChildren(Pane parent) {
        ObservableList<Node> children = parent.getChildren();
        for (Node node : children) {
            if (node instanceof Pane) {
                scanChildren((Pane) node);
            } else if (node instanceof VBox) {
                scanChildren((VBox) node);
            } else if (node.getUserData()!=null) {
                String userData = (String)node.getUserData();
                String[] flows = userData.split("--");
                if ( flows.length > 1 ) {
                    String observable= flows[0];
                    String subscriber= flows[1];
                    if ( subscriber.compareTo("RedGreen")==0) {
                        log.info("RedGreen on "+observable);
                        topicSubscriber.put(observable,new RedGreen(node));
                    } else if ( subscriber.compareTo("SetValue")==0) {
                        log.info("Gauge on "+observable);
                        topicSubscriber.put(observable,new SetValue((Gauge)node));
                    } else if ( subscriber.compareTo("SetText")==0) {
                        log.info("Gauge on "+observable);
                        topicSubscriber.put(observable,new SetText((Label)node));
                    }
                } else {
                    log.warn(" flow too short : "+userData);
                }
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log.info("Controller started " + url + "  " + resourceBundle);
        scanChildren(anchorPane);
        mqttIsConnected.subscribe((Subscriber<Boolean>)topicSubscriber.get("mqttConnected"));
        mqttConnect();
        Long time = System.currentTimeMillis();

    }


    void mqttConnect() {
        try {
            log.info("mqtt connecting...");
            String tmpDir = System.getProperty("java.io.tmpdir");
            MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
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
        Subscriber<?> subscriber = topicSubscriber.get(topic);
        if (subscriber == null) return;
        Object json = new JSONTokener(message).nextValue();
        if (json instanceof Boolean) {
            ((Subscriber<Boolean>) subscriber).onNext((Boolean) json);
        } else if (json instanceof Double) {
            ((Subscriber<Double>) subscriber).onNext((Double) json);
        } else if (json instanceof Integer) {
            ((Subscriber<Double>) subscriber).onNext(new Double((Integer) json));
        }  else if (json instanceof String) {
            ((Subscriber<String>) subscriber).onNext((String) json);
        } else {
            log.info(" unhandled type "+json.getClass());
        }

        if (topic.startsWith("src/drive/motor")) {
            //log.info(" MQTT RCV " + topic + ":" + message);
        } else return;
        if (topic.compareTo(motorRpmTarget.getUserData().toString()) == 0) {
            Platform.runLater(() -> {
                motorRpmTarget.setValue(Double.parseDouble(message));
            });
        }
        if (topic.compareTo(motorRpmMeasured.getUserData().toString()) == 0)
            Platform.runLater(() -> {
                motorRpmMeasured.setValue(Double.parseDouble(message));
            });
        if (topic.compareTo(motorCurrent.getUserData().toString()) == 0)
            Platform.runLater(() -> {
                motorCurrent.setValue(Double.parseDouble(message));
            });
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
