package sample.cluster.akka;

import akka.actor.AbstractLoggingActor;

public class ErrorActor extends AbstractLoggingActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ErrorExceprion.class, x -> {
                    log().error(x.getErrorException(),
                            "Actor {} get Exception ,Message is {} ,ErrorTime is {},Object is {}",
                            x.getErrorActor().path(),
                            x.getErrorMessage(),
                            x.getErrorTime(),
                            x.getErrorObject());

                    //TODO
                    //insert into db
                }
        ).matchAny(x -> {
            log().error("unknowMessage");
        })
                .build();
    }
}
