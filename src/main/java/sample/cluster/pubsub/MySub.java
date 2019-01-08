package sample.cluster.pubsub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ConsistentHashingRouter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import sample.cluster.simple.Launcher;

import java.util.HashMap;

public class MySub extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public MySub() {
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // subscribe to the topic named "kafka"
        mediator.tell(new DistributedPubSubMediator.Subscribe("kafka", "groupId", getSelf()), getSelf());
    }

    public static Props props() {
        return Props.create(MySub.class, MySub::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ImmutablePair.class, x -> {
                    //log.info("topic:{},value:{},thisis:{}", x.left, x.right, getContext().system().settings().config().getString("akka.remote.artery.canonical.port"));
                    getContext().getSystem().actorSelection("/user/workerRouter" + Launcher.topicMap.get(x.left)).tell(new ConsistentHashingRouter.ConsistentHashableEnvelope(x, x.right), getSelf());
                })
                .match(DistributedPubSubMediator.SubscribeAck.class, x -> log.info("subscribing"))
                .matchAny(x -> log.info("getUnknowMessage"))
                .build();
    }
}
