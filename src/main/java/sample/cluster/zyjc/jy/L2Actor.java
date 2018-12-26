package sample.cluster.zyjc.jy;

import akka.actor.AbstractLoggingActor;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class L2Actor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(
                ImmutablePair.class, x -> log().info("this is L2,topic is {},value is {}", x.left, x.right)
        ).matchAny(x -> log().info("unknowMessage")).build();
    }
}
