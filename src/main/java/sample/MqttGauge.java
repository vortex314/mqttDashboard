package sample;

import eu.hansolo.medusa.Gauge;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import static be.limero.dashboard.Util.now;

public class MqttGauge extends Gauge implements MqttTopic, Subscriber<Double> {
    String topic="NO_TOPIC";
    Long setTime;
    Integer timeout=Integer.MAX_VALUE;;
    Timer timer=new Timer(true);
    private static final Logger log
            = LoggerFactory.getLogger(MqttGauge.class);

    public MqttGauge() {
        super(SkinType.GAUGE);
        setTime=now();
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getTimeout() {
        return timeout;
    }
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if ( (timedout())) {
                    log.info(getTopic()+" delta : "+((now()-setTime)+" > "+timeout));
                    if (timedout()) setStyle("-fx-background-color: #FF0000;");
                    else setStyle("-fx-background-color: #00FF00;");
                }
            }
        }, timeout, timeout);
    }

    Boolean timedout() {
        return  (now()-setTime) > timeout;
    }

    @Override
    public void onSubscribe(Subscription subscription) {

    }

    @Override
    public void onNext(Double d) {
        setTime=now();
       setValue(d);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
