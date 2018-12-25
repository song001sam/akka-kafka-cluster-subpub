package sample.cluster.simple;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import sample.cluster.kafka.MyConsumer;
import sample.cluster.kafka.MyProducer;
import sample.cluster.pubsub.MySub;

import java.util.HashMap;
import java.util.Map;

public class Launcher {
    public static final Map<String, ActorSystem> systemMap = new HashMap<>();

    public static void startup(String[] ports) {
        for (String port : ports) {
            Config config =
                    ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port)
                            .withFallback(ConfigFactory.load());
            ActorSystem system = ActorSystem.create("ClusterSystem", config);
            systemMap.put(port, system);
            ActorRef actor1 = system.actorOf(Props.create(MyConsumer.class), "MyConsumer");
            actor1.tell(new MyConsumer.StartConsumer(), ActorRef.noSender());
            system.actorOf(Props.create(MySub.class), "sub1");
        }
        systemMap.get("2551").actorOf(Props.create(MyProducer.class), "MyProducer").tell(new MyProducer.StartProduce(), ActorRef.noSender());
    }
}
