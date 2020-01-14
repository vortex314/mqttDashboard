package be.limero.dashboard;

import lombok.Getter;
import lombok.Setter;

public interface MqttProperty<T extends Object>  {
    public static String srcPrefix="src/";
    public static String dstPrefix="dst/";

    void setSrc(String s);
    String getSrc();

    void setDst(String s);
    String getDst();


    void setDisableTimeout(Integer timeout);

    Integer getDisableTimeout();

    void setQos(Integer qos);

    Integer getQos();

    void setRetained(Boolean retained);

    Boolean getRetained();

    void setMqtt(Mqtt client);

    Mqtt getMqtt();

    void onNext(Object value);

}


