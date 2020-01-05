package be.limero.dashboard;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import akka.stream.Materializer;
import akka.stream.Supervision;
import akka.stream.alpakka.mqtt.MqttConnectionSettings;
import akka.stream.alpakka.mqtt.MqttMessage;
import akka.stream.alpakka.mqtt.MqttQoS;
import akka.stream.alpakka.mqtt.MqttSubscriptions;
import akka.stream.alpakka.mqtt.javadsl.MqttSource;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Unit;

import java.lang.reflect.Array;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class SourceMqtt<T> {
    private static final Logger log
            = LoggerFactory.getLogger(SourceMqtt.class);
    static ActorSystem system = ActorSystem.create("MySystem");
    static Materializer materializer = ActorMaterializer.create(system);
    static MqttConnectionSettings connectionSettings =
            MqttConnectionSettings.create(
                    "tcp://pi3.local:1883", // (1)
                    "test-java-client" + System.currentTimeMillis(), // (2)
                    new MemoryPersistence() // (3)
            );
    static MqttSubscriptions subscriptions =
            MqttSubscriptions.create("src/#", MqttQoS.atMostOnce())
                    .addSubscription("dst/#", MqttQoS.atMostOnce());

    public static void main(String[] args) {

        Source<MqttMessage, CompletionStage<Done>> mqttSource =
                MqttSource.atMostOnce(
                        connectionSettings.withClientId("source-test/source"), subscriptions, 1);
        Pair<CompletionStage<Done>, CompletionStage<List<String>>> materialized =
                mqttSource
                        .map(m -> m.topic() + "-" + m.payload().utf8String())
                        .log("topic")
                        .take(100)
                        .toMat(Sink.seq(), Keep.both())
                        .run(materializer);

        CompletionStage<Done> subscribed = materialized.first();
        CompletionStage<List<String>> streamResult = materialized.second();
        streamResult.handle((List<String> list, Throwable thr) -> {
            log.info(list.get(0));
            return null;
        });
        try {
            log.info("going to sleep");
            Thread.sleep(10000);
        } catch (Exception ex) {
            log.warn("exit", ex);
        }
        system.terminate();
    }

    Source<T, CompletionStage<Done>> source(String topic) {
        Source<MqttMessage, CompletionStage<Done>> mqttSource =
                MqttSource.atMostOnce(
                        connectionSettings.withClientId("source-test/source"), MqttSubscriptions.create(topic,MqttQoS.atMostOnce()), 1);
        Source<T, CompletionStage<Done>> v = mqttSource.map(m -> m.topic() + "-" + m.payload().utf8String()).map(str ->(T) new JSONTokener(str).nextValue());
        return v;
    }
}
