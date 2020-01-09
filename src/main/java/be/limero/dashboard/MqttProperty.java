package be.limero.dashboard;

import org.reactivestreams.Subscriber;
import sun.awt.SubRegionShowable;

public interface MqttProperty<T> extends Subscriber<T> {
    void setTopic(String topic);

    String getTopic();

    void setDisableTimeout(Integer timeout);

    Integer getDisableTimeout();

    void setQos(Integer qos);

    Integer getQos();

    void setRetained(Boolean retained);

    Boolean getRetained();

    void setMqtt(Mqtt client);

    Mqtt getMqtt();


}
