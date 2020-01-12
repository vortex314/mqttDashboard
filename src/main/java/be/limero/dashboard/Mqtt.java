package be.limero.dashboard;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONTokener;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class Mqtt implements IMqttMessageListener {
    private static final Logger log
            = LoggerFactory.getLogger(Mqtt.class);

    public ValuePublisher<Boolean> connected = new ValuePublisher<>();

    MqttAsyncClient mqttClient;
    MqttConnectOptions mqttConnectOptions;
    HashMap<String, Subscriber<Object>> topicSubscribers = new HashMap<String, Subscriber<Object>>();

    void register(MqttProperty mqttProperty) {
        topicSubscribers.put(mqttProperty.getTopic(), mqttProperty);
    }

    void connect() {
        try {
            log.info("mqtt connecting...");
     /*       String tmpDir = System.getProperty("java.io.tmpdir");
            MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);*/
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttClient = new MqttAsyncClient("tcp://limero.ddns.net:1883", "Paho" + System.nanoTime());
            mqttClient.connect(mqttConnectOptions, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    subscribe("src/#", 0);
                    connected.set(true);
                    log.info("mqtt connected.");
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    log.warn("connection failed. ", throwable);
                    connected.set(false);
                }
            });
        } catch (Exception ex) {
            log.warn("mqtt connect failed. ", ex);
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
            log.info(" PUBLISH " + topic + " : " + message);
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(0);
            mqttMessage.setRetained(false);
            mqttClient.publish(topic, mqttMessage);
        } catch (MqttException ex) {
            log.warn("publish issue ", ex);
        }
    }

    void subscribe(String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    log.info("subscribed.");
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    log.warn("subscribe issue. ", throwable);
                }
            });
        } catch (MqttException ex) {
            log.warn("subscribe issue ", ex);
        }
    }

    // @Override
    public void connectionLost(Throwable throwable) {
        connected.set(false);
        log.warn("MQTT connection lost ");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        Object json = new JSONTokener(message).nextValue();
        Subscriber<Object> subscriber = topicSubscribers.get(topic);
        if (subscriber != null)
            subscriber.onNext(mqttMessage);
    }
}
