package be.limero.dashboard;

import eu.hansolo.medusa.Gauge;
import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sample.MqttTopic;

import java.util.Timer;
import java.util.TimerTask;

import static be.limero.dashboard.Util.now;

public class MqttGauge extends Gauge implements MqttProperty, Subscriber<Double> {
    @Setter @Getter String topic;
    @Setter @Getter Boolean retained;
    @Setter @Getter Integer qos;
    @Setter @Getter Integer disableTimeout=Integer.MAX_VALUE;
    @Setter @Getter Mqtt mqtt;

    Long setTime;
    Timer timer=new Timer(true);
    private static final Logger log
            = LoggerFactory.getLogger(MqttGauge.class);

    public MqttGauge() {
        super(SkinType.GAUGE);
        setTime=now();
    }


    public void setTimeout(Integer timeout) {
        this.disableTimeout = timeout;
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
        return  (now()-setTime) > disableTimeout;
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
