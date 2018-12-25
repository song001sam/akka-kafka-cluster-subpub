package sample.cluster.kafka;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

public class MyProducer extends AbstractActor {
    KafkaProducer<String, String> producer;
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
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
        producer = new KafkaProducer<>(props);
    }

    public void mostOnce() {
        try {
            while (flag) {
                ProducerRecord<String, String> record = new ProducerRecord<>("test2", Integer.toString(new Random().nextInt()), Integer.toString(new Random().nextInt()));
                producer.send(record);
                record = new ProducerRecord<>("test1", Integer.toString(new Random().nextInt()), Integer.toString(new Random().nextInt()));
                producer.send(record);
                Thread.sleep(1000);
            }
        } catch (
                Exception e
        ) {

        }

    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(StartProduce.class, x -> {
                    mostOnce();
                })
                .match(StopProduce.class, x -> {
                    flag = false;
                })
                .build();
    }

    public static class StartProduce {
    }

    public static class StopProduce {
    }
}
