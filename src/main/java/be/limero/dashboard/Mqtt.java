package be.limero.dashboard;

import io.reactivex.Observable;
import io.reactivex.Observer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.json.simple.JSONValue;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.collection.mutable.MultiMap;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class Mqtt implements MqttCallback {
    private static final Logger log
            = LoggerFactory.getLogger(Mqtt.class);
    MultiValuedMap<String, PubMsgHandler> mapSubscribers = new ArrayListValuedHashMap<>();

    @Setter
    @Getter
    public BooleanProperty connected = new SimpleBooleanProperty();

    @Setter
    @Getter
    public static Mqtt thisMqtt = null;

    public void onMqtt(String topic, Observer<PubMsg> subscriber) {

    }


    class SubscriberInfo {
        public MqttProperty<Object> mqttProperty;
        public String topic;
        public Long lastUpdated;
    }

    ;


    MqttAsyncClient mqttClient;
    MqttConnectOptions mqttConnectOptions;
    HashMap<String, MqttProperty<Object>> topicSubscribers = new HashMap<String, MqttProperty<Object>>();

    public Mqtt() {
        connected.set(false);
        thisMqtt = this;
    }


    void register(String topic, PubMsgHandler handler) {
        mapSubscribers.put(topic, handler);
    }

    void register(MqttProperty mqttProperty) {
        topicSubscribers.put(mqttProperty.getSrc(), mqttProperty);
    }

    public void connect() {
        try {
            log.info("mqtt connecting...");
            /*       String tmpDir = System.getProperty("java.io.tmpdir");*/
            MemoryPersistence dataStore = new MemoryPersistence();
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttClient = new MqttAsyncClient("tcp://limero.ddns.net:1883", "Paho" + System.nanoTime(), dataStore);
            mqttClient.setCallback(this);
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
            log.info("disconnect()");
            mqttClient.disconnect();
        } catch (MqttException ex) {
            log.warn("disconnect issue ", ex);
        }
    }

    void publish(String topic, Object message) {
        JSONValue.toJSONString(message);
        mqttPublish(topic, JSONValue.toJSONString(message));
    }

    void mqttPublish(String topic, String message) {
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
        connect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        Object json = new JSONTokener(message).nextValue();
        if (topic.startsWith("src/")) {
            Collection<PubMsgHandler> handlerList = mapSubscribers.get(topic);
            for (PubMsgHandler handler : handlerList) {
                    handler.onPubMsg(topic, json);
                }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void stop() {
        try {
            mqttClient.disconnect();
            mqttClient.close(true);
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
}
