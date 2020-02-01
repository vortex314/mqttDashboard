package be.limero.dashboard;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONTokener;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.HashMap;

public class Mqtt implements MqttCallback {
    private static final Logger log
            = LoggerFactory.getLogger(Mqtt.class);
    MultiValuedMap<String, PubMsgHandler> mapSubscribers = new ArrayListValuedHashMap<>();

    public BooleanProperty connected = new SimpleBooleanProperty();

    private String url = "tcp://test.mosquitto.org";

    class SubscriberInfo {
        public MqttProperty<Object> mqttProperty;
        public String topic;
        public Long lastUpdated;
    }

    MqttAsyncClient mqttClient;
    MqttConnectOptions mqttConnectOptions;
    HashMap<String, MqttProperty<Object>> topicSubscribers = new HashMap<String, MqttProperty<Object>>();

    public Mqtt() {
        connected.set(false);
    }


    void register(String topic, PubMsgHandler handler) {
        if (connected.get()) subscribe(topic, 0);
        mapSubscribers.put(topic, handler);
    }

    void register(MqttProperty mqttProperty) {
        topicSubscribers.put(mqttProperty.getSrc(), mqttProperty);
    }

    void subscribeRegistered() {
        MultiSet<String> topics = mapSubscribers.keys();
        for (String topic : topics) {
            subscribe(topic, 0);
        }
    }

    public void connect() {
        try {
            log.info("mqtt connecting...");
            /*       String tmpDir = System.getProperty("java.io.tmpdir");*/
            MemoryPersistence dataStore = new MemoryPersistence();
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttClient = new MqttAsyncClient(url, "mqttDashboard" + System.nanoTime(), dataStore);
            mqttClient.setCallback(this);
            mqttClient.connect(mqttConnectOptions, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    //                   subscribe("src/#", 0);
                    connected.set(true);
                    log.info("mqtt connected.");
                    subscribeRegistered();
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
                    log.info("subscribed " + topic);
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

    public boolean isConnected() {
        return connected.get();
    }

    public BooleanProperty connectedProperty() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
