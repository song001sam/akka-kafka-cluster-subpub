package sample.cluster.simple;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import sample.cluster.akka.DeviceGroup;
import sample.cluster.akka.DeviceManager;
import sample.cluster.kafka.MyConsumer;
import sample.cluster.pubsub.MySub;

public class SimpleClusterApp {

    public static void main(String[] args) {
        if (args.length == 0) startup(new String[]{"2551", "2552"});
        else startup(args);
    }

    private static void startup(String[] ports) {
        Config config =
                ConfigFactory.parseString("akka.remote.artery.canonical.port=" + ports[0])
                        .withFallback(ConfigFactory.load());
        ActorSystem system1 = ActorSystem.create("ClusterSystem", config);
        system1.actorOf(Props.create(SimpleClusterListener.class), "clusterListener");
        ActorRef actor1 = system1.actorOf(Props.create(MyConsumer.class));
        actor1.tell(new MyConsumer.StartConsumer(), ActorRef.noSender());
        ActorRef sub1 = system1.actorOf(Props.create(MySub.class), "sub1");
        config =
                ConfigFactory.parseString("akka.remote.artery.canonical.port=" + ports[1])
                        .withFallback(ConfigFactory.load());
        ActorSystem system2 = ActorSystem.create("ClusterSystem", config);

        system2.actorOf(Props.create(SimpleClusterListener.class), "clusterListener");
        ActorRef sub2 = system2.actorOf(Props.create(MySub.class), "sub2");

    }
}
