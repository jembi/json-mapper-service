package org.jembi.ciol.jsonMapper;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import ch.megard.akka.http.cors.javadsl.settings.CorsSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.ciol.AppConfig;
import org.jembi.ciol.RestConfig;
import org.jembi.ciol.models.GlobalConstants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;

import static ch.megard.akka.http.cors.javadsl.CorsDirectives.cors;
import static org.jembi.ciol.jsonMapper.ValidateFile.validateFile;

public class HttpServer extends AllDirectives {

    private static final Logger LOGGER = LogManager.getLogger(HttpServer.class);
    private CompletionStage<ServerBinding> binding = null;

    public static String readJsonFile(final String fileName) {
        String jsonData = "";
        LOGGER.debug(fileName);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonData += line + "\n";
            }
            bufferedReader.close();
        } catch (Exception e) {
            LOGGER.error("Couldn't read configuration file");
            e.printStackTrace();
        }
        return jsonData;
    }

    public HttpResponse loadReportData(String jsonStr) {
        return validateFile(jsonStr);
    }

    public HttpResponse updateConfig(String jsonStr) {
        try {
            FileWriter file = new FileWriter(GlobalConstants.METADATA_FILE_PATH);
            file.write(jsonStr);
            file.close();
        } catch (IOException e) {
            LOGGER.debug("Failed to update Config File - BAD_REQUEST " + e.getMessage());
            return HttpResponse.create().withStatus(StatusCodes.BAD_REQUEST);
        }
        return HttpResponse.create().withStatus(StatusCodes.OK);
    }

    public void returnConfig() {
        System.out.println("AAAHHH");
    }

    void close(ActorSystem<Void> system) {
        binding.thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }

    void open(final ActorSystem<Void> system,
              final ActorRef<BackEnd.Event> backEnd) {
        final Http http = Http.get(system);
        HttpServer app = new HttpServer();
        binding = http.newServerAt(AppConfig.HTTP_SERVER_HOST,
                        AppConfig.HTTP_SERVER_PORT)
                .bind(app.createRoute(system, backEnd));
        LOGGER.info("Server online at http://{}:{}", AppConfig.HTTP_SERVER_HOST, AppConfig.HTTP_SERVER_PORT);
    }

    private Route setConfig(final ActorSystem<Void> actorSystem,
                            final ActorRef<BackEnd.Event> backEnd,
                            final RestConfig config) {
        LOGGER.debug("{}", config);

        CompletionStage<BackEnd.EventSetConfigRsp> result =
                AskPattern.ask(backEnd,
                        replyTo -> new BackEnd.EventSetConfig(config, replyTo),
                        java.time.Duration.ofSeconds(3),
                        actorSystem.scheduler());
        var completableFuture = result.toCompletableFuture();
        try {
            var reply = completableFuture.get(4, TimeUnit.SECONDS);
            return complete(reply.responseCode());
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        return complete(StatusCodes.IM_A_TEAPOT);
    }

    private Route createRoute(final ActorSystem<Void> actorSystem,
                              final ActorRef<BackEnd.Event> backEnd) {
        LOGGER.debug("Arrived at createRoute");
        final var settings = CorsSettings.defaultSettings().withAllowGenericHttpRequests(true);
        return cors(settings,
                () -> pathPrefix("CIOL",
                        () -> concat(
                                post(() -> concat(
                                                path("installNewConfig",
                                                        () -> entity(Jackson.unmarshaller(RestConfig.class),
                                                                config -> setConfig(actorSystem, backEnd, config))),
                                                path("reports",
                                                        () -> {
                                                            LOGGER.debug("Check out the reports life");
                                                            return entity(Unmarshaller.entityToString(),
                                                                    json -> completeWithFuture(
                                                                            CompletableFuture.supplyAsync(
                                                                                    () -> loadReportData(json)
                                                                            )
                                                                    )
                                                            );
                                                        }
                                                ),
                                                path("config",
                                                        () -> {
                                                            LOGGER.debug("config");
                                                            return entity(Unmarshaller.entityToString(),
                                                                    config -> {
                                                                        LOGGER.debug("Check out the config life");
                                                                        return completeWithFuture(
                                                                                CompletableFuture.supplyAsync(
                                                                                        () -> updateConfig(config)
                                                                                )
                                                                        );
                                                                    }
                                                            );
                                                        }
                                                )
                                        )
                                ),
                                get(() -> concat(
                                                path("mapperconfig",
                                                        () -> {
                                                            final String jsonConfig = readJsonFile(
                                                                    "/app/conf/MetadataConfigURL.json");
                                                            return complete(jsonConfig);
                                                        })
                                ))
                        )
                ));

    }
}