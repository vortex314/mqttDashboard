package be.limero.dashboard;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class Mqtt implements MqttCallback {
    private static final Logger log
            = LoggerFactory.getLogger(Mqtt.class);

    MqttClient mqttClient;
    MqttConnectOptions mqttConnectOptions;
    HashMap<String, Observer<MqttMessage>> topicObserver = new HashMap<String, Observer<MqttMessage>>();

    void connect() {
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
        } catch (Exception ex) {
            log.warn("failed ", ex);
        }
    }

    public void disconnect() {
        try {
            mqttClient.disconnect();
        } catch (MqttException ex) {
            log.warn("disconnect issue ", ex);
        }
    }

    void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(0);
            mqttMessage.setRetained(false);
            mqttClient.publish(topic, mqttMessage);
        } catch (MqttException ex) {
            log.warn("publish issue ", ex);
        }
    }

    void subscribe(String topic,int qos){
        try {
            mqttClient.subscribe(topic,qos);
        } catch (MqttException ex) {
            log.warn("subscribe issue ", ex);
        }
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
}
