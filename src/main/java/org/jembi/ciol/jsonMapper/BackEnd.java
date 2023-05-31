package org.jembi.ciol.jsonMapper;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.ciol.RestConfig;

public class BackEnd extends AbstractBehavior<BackEnd.Event> {

    private static final Logger LOGGER = LogManager.getLogger(BackEnd.class);

    private BackEnd(ActorContext<Event> context) {
        super(context);
        context.getSystem()
               .dispatchers()
               .lookup(DispatcherSelector.fromConfig("input-blocking-dispatcher"));
    }

    public static Behavior<BackEnd.Event> create() {
        return Behaviors.setup(BackEnd::new);
    }


    @Override
    public Receive<Event> createReceive() {
        ReceiveBuilder<BackEnd.Event> builder = newReceiveBuilder();
        return builder
                .onMessage(EventSetConfig.class, this::eventSetConfigHandler)
                .build();
    }

    private Behavior<Event> eventSetConfigHandler(EventSetConfig event) {
        LOGGER.info("{}",  event.restConfig);
        if ("input_service".equals(event.restConfig.appID())){
            event.replyTo.tell(new EventSetConfigRsp(StatusCodes.OK));
        }else{
            LOGGER.debug("Wrong service");
            event.replyTo.tell(new EventSetConfigRsp(StatusCodes.IM_A_TEAPOT));
        }
        return Behaviors.same();
    }

    interface Event { }

    interface EventResponse { }

    public record EventSetConfig(
            RestConfig restConfig,
            ActorRef<EventSetConfigRsp> replyTo) implements Event {
    }

    public record EventSetConfigRsp(StatusCode responseCode) implements EventResponse { }
}
