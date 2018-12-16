package sample.cluster.kafka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.*;

public class MyConsumer extends AbstractActor {
    KafkaConsumer<String, String> consumer;
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    boolean flag = true;

    @Override
    public void preStart() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test-1");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        consumer = new KafkaConsumer<>(props);
    }

    public void alwaysConsumer() {
        try {
            consumer.subscribe(Arrays.asList("test"));
            while (flag) {
                consumer.poll(Duration.ofSeconds(1)).forEach(x -> {
                    mediator.tell(new DistributedPubSubMediator.Publish("content", x.value(), true), getSelf());
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
