package sample.cluster.akka;


import akka.actor.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
public class ErrorExceprion extends Exception {
    private Object errorObject;
    private String errorMessage;
    private Exception errorException;
    private Date errorTime;
    private ActorRef errorActor;
}
