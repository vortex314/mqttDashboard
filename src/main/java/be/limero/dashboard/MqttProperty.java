package be.limero.dashboard;

public interface MqttProperty<T extends Object>  {
    public static String srcPrefix="src/";
    public static String dstPrefix="dst/";

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

    void onNext(Object value);

}


