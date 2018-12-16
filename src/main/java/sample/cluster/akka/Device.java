package sample.cluster.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Device extends AbstractActor {
    final String groupId;
    final String topic;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public Device(String groupId, String topic) {
        this.groupId = groupId;
        this.topic = topic;
    }

    public static Props props(String groupId, String deviceId) {
        return Props.create(Device.class, () -> new Device(groupId, deviceId));
    }

    @Override
    public void preStart() {
        log.info("Device actor {}-{} started", groupId, topic);
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // subscribe to the topic named "content"
        mediator.tell(new DistributedPubSubMediator.Subscribe(topic, getSelf()), getSelf());
    }

    @Override
    public void postStop() {
        log.info("Device actor {}-{} stopped", groupId, topic);
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // subscribe to the topic named "content"
        mediator.tell(new DistributedPubSubMediator.Unsubscribe(topic, getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        DeviceManager.RequestTrackDevice.class,
                        r -> {
                            if (this.groupId.equals(r.groupId) && this.topic.equals(r.deviceId)) {
                                getSender().tell(new DeviceManager.DeviceRegistered(), getSelf());
                            } else {
                                log.warning(
                                        "Ignoring TrackDevice request for {}-{}.This actor is responsible for {}-{}.",
                                        r.groupId,
                                        r.deviceId,
                                        this.groupId,
                                        this.topic);
                            }
                        })
                .match(
                        String.class,
                        x ->
                                log.info(
                                        "Got: {},i am:{}",
                                        x,
                                        getContext()
                                                .system()
                                                .settings()
                                                .config()
                                                .getString("akka.remote.artery.canonical.port")))
                .match(DistributedPubSubMediator.SubscribeAck.class, x -> log.info("subscribing"))
                .build();
    }
}
