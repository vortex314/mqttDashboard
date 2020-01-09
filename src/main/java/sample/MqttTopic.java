package sample;

import org.reactivestreams.Subscriber;

public interface MqttTopic<T> {
    public void setTopic(String topic);

    public String getTopic();
}
