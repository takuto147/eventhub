package com.example.eventhub.worker;


import com.example.eventhub.config.AmqpConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class DiscordWorker {
private final WebClient webClient = WebClient.create();
private final ObjectMapper om = new ObjectMapper();


@Value("${app.discord-webhook-url}")
private String discordUrl;


@RabbitListener(queues = AmqpConfig.QUEUE_MAIN)
public void handle(String payload) throws Exception {
JsonNode root = om.readTree(payload);
String id = root.path("id").asText("(no-id)");
String msg = root.path("message").asText("(no-message)");
String content = "\uD83D\uDFE2 Event " + id + ": " + msg; // 緑丸


var res = webClient.post()
.uri(discordUrl)
.contentType(MediaType.APPLICATION_JSON)
.bodyValue("{" + "\"content\":\"" + content.replace("\"","\\\"") + "\"}")
.exchangeToMono(r -> {
int s = r.statusCode().value();
if (s >= 200 && s < 300) return Mono.just(true);
if (s == 429 || s >= 500) return Mono.error(new RuntimeException("retryable status: " + s));
return Mono.error(new IllegalStateException("non-retryable status: " + s));
})
.block();
}
}