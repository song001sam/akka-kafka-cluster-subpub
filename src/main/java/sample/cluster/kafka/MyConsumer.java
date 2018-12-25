package sample.cluster.kafka;

import akka.Done;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.kafka.*;
import akka.kafka.javadsl.Committer;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.typesafe.config.Config;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

public class MyConsumer extends AbstractActor {
    KafkaConsumer<String, String> consumer;
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    String[] topics;
    boolean flag = true;
    @Override
    public void preStart() {
        Properties props = new Properties();
        try {
            props.load(MyConsumer.class.getClassLoader().getResourceAsStream("topic.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        topics = props.getProperty("topics").split(",");
        consumer = new KafkaConsumer<>(props);
    }
    public void alwaysConsumer() {
        try {
            consumer.subscribe(Arrays.asList(topics));
            while (flag) {
                consumer.poll(Duration.ofSeconds(1)).forEach(x -> {
                    mediator.tell(new DistributedPubSubMediator.Publish("kafka", new ImmutablePair<>(x.topic(), x.value()), true), getSelf());
                });
            }
        } catch (
                Exception e
        ) {

        }
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(StartConsumer.class, x -> {
                    log.info("startConsumer");
                    alwaysConsumer();
                })
                .match(StopConsumer.class, x -> {
                    log.info("stopConsumer");
                    flag = false;
                }).match(
                        String.class, x -> {
                            if (x.equals("stop"))
                                flag = false;
                        }
                )
                .build();
    }

    public static class StartConsumer {
    }

    public static class StopConsumer {
    }
}
