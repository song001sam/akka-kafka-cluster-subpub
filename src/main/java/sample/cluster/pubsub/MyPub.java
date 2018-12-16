//package sample.cluster.pubsub;
//
//import akka.actor.AbstractActor;
//import akka.actor.ActorRef;
//import akka.cluster.pubsub.DistributedPubSub;
//import akka.cluster.pubsub.DistributedPubSubMediator;
//
//public class MyPub extends AbstractActor {
//    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
//
//    @Override
//    public Receive createReceive() {
//        return receiveBuilder().match(String.class,x -> {
//            mediator.tell(new DistributedPubSubMediator.Publish("content",x),getSelf());
//        }).build();
//    }
//}
