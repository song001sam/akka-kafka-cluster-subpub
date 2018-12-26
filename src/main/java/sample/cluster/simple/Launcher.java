package sample.cluster.simple;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.routing.ClusterRouterPool;
import akka.cluster.routing.ClusterRouterPoolSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.routing.ConsistentHashingPool;
import akka.routing.ConsistentHashingRouter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import sample.cluster.kafka.MyConsumer;
import sample.cluster.kafka.MyProducer;
import sample.cluster.pubsub.MySub;
import sample.cluster.zyjc.jy.L1Actor;

import java.util.*;

public class Launcher {
    public static final Map<String, ActorSystem> systemMap = new HashMap<>();
    public static final Map<String, String> topicMap = new HashMap<>();
    public static void startup(String[] ports) {
        for (String port : ports) {
            Config config =
                    ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port)
                            .withFallback(ConfigFactory.load("topic"));
            ActorSystem system = ActorSystem.create("ClusterSystem", config);
            systemMap.put(port, system);
            ActorRef actor1 = system.actorOf(Props.create(MyConsumer.class), "MyConsumer");
            actor1.tell(new MyConsumer.StartConsumer(), ActorRef.noSender());
            system.actorOf(Props.create(MySub.class), "sub1");
            ///////
            Set<String> useRoles = new HashSet<>(Arrays.asList("compute"));
            for (Config configtemp : config.getConfigList("topics.topics")) {
                for (String topic : configtemp.getStringList("topics")) {
                    topicMap.put(topic, configtemp.getString("name"));
                }
                try {
                    ActorRef workerRouter = system.actorOf(
                            new ClusterRouterPool(new ConsistentHashingPool(10),
                                    new ClusterRouterPoolSettings(config.getInt("topics.totalInstances"), config.getInt("topics.maxInstancesPerNode"),
                                            config.getBoolean("topics.allowLocalRoutees"), useRoles)).props(Props
                                    .create(Class.forName(configtemp.getString("class")))), "workerRouter" + configtemp.getString("name"));
                    //System.out.println(workerRouter.path());
                    //workerRouter.tell(new ConsistentHashingRouter.ConsistentHashableEnvelope(new ImmutablePair<>("123","456"),"123"),ActorRef.noSender());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }
        systemMap.get("2551").actorOf(Props.create(MyProducer.class), "MyProducer").tell(new MyProducer.StartProduce(), ActorRef.noSender());
    }
}
