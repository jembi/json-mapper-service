package org.jembi.ciol.input;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.ciol.AppConfig;

import org.jembi.ciol.kafka.MyKafkaProducer;
import org.jembi.ciol.models.GlobalConstants;
import org.jembi.ciol.models.NotificationMessage;
import org.jembi.ciol.models.Payload;

import java.nio.file.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public final class Input {

    private static final Logger LOGGER = LogManager.getLogger(Input.class.getName());
    private static final Object MONITOR = new Object();
    private static MyKafkaProducer<String, Payload> payloadProducer;
    private static MyKafkaProducer<String, NotificationMessage> notificationProducer;
    private static final Path csvDir = Paths.get("/app/csv");
    private HttpServer httpServer;

    public static void main(final String[] args) {
        new Input().run();
    }

    public static void sendToPayload(Payload payload) {
        try {
            var s = payloadProducer.produceSync(payload.dataset(), payload);
            LOGGER.info("{}", s);
            LOGGER.info("Record: {} sent to {}", payload, GlobalConstants.TOPIC_PAYLOAD_QUEUE);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public static void sendNotification(NotificationMessage notificationMessage){
        try {
            var s = notificationProducer.produceSync(notificationMessage.source(), notificationMessage);
            LOGGER.info("{}", s);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    ActorRef<BackEnd.Event> backEnd = context.spawn(BackEnd.create(), "BackEnd");
                    context.watch(backEnd);
                    httpServer = new HttpServer();
                    httpServer.open(context.getSystem(), backEnd);
                    LOGGER.debug("Server up and running: {}", httpServer.toString());
                    return Behaviors.receive(Void.class)
                            .onSignal(akka.actor.typed.Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    private void run() {
        LOGGER.info("KAFKA: {} {} {}",
                AppConfig.KAFKA_BOOTSTRAP_SERVERS,
                AppConfig.KAFKA_APPLICATION_ID,
                AppConfig.KAFKA_CLIENT_ID);

        payloadProducer = new MyKafkaProducer<>(AppConfig.KAFKA_CLIENT_ID + "-payload", GlobalConstants.TOPIC_PAYLOAD_QUEUE);
        notificationProducer = new MyKafkaProducer<>(AppConfig.KAFKA_CLIENT_ID + "-notifications", GlobalConstants.TOPIC_NOTIFICATIONS);
        ActorSystem.create(this.create(), "InputApp");

        synchronized (MONITOR) {
            try {
                MONITOR.wait();
            } catch (InterruptedException e) {
                LOGGER.error(e.toString());
            }
        }
    }
}