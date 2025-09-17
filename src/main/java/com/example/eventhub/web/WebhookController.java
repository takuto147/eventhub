package com.example.eventhub.web;


import com.example.eventhub.model.InboundEvent;
import com.example.eventhub.mq.EventPublisher;
import com.example.eventhub.repo.InboundEventRepository;
import com.example.eventhub.security.HmacVerifier;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/webhooks")
public class WebhookController {
private final InboundEventRepository repo;
private final EventPublisher publisher;
private final HmacVerifier hmac;


public WebhookController(InboundEventRepository repo, EventPublisher publisher, HmacVerifier hmac) {
this.repo = repo; this.publisher = publisher; this.hmac = hmac;
}


@PostMapping("/{source}")
@Transactional
public ResponseEntity<?> receive(@PathVariable String source,
@RequestBody String rawBody,
@RequestHeader(name = "X-Signature", required = false) String sig) {
// 1) 署名検証
boolean ok = (sig != null) && hmac.verify(rawBody, sig);


// 2) event_id を JSON から素朴に抽出（簡略。実務はJSONパース推奨）
String eventId = rawBody.replaceAll(".*\"id\"\s*:\s*\"([^\"]+)\".*", "$1");
if (eventId == null || eventId.isBlank()) eventId = String.valueOf(rawBody.hashCode());


// 3) 冪等性（同一イベントは200で無害に返す）
if (repo.findBySourceAndEventId(source, eventId).isPresent()) {
return ResponseEntity.ok().build();
}


// 4) 保存
InboundEvent e = new InboundEvent();
e.setSource(source);
e.setEventId(eventId);
e.setPayload(rawBody);
e.setSignatureOk(ok);
repo.save(e);


// 5) キューへ
publisher.publish(source, rawBody);
return ResponseEntity.accepted().build();
}
}



//-------------------memo--------------------------------
@PostMapping("/{source}")
@Transactional
public ResponseEntity<?> receive(
    @PathVariable String source,     // URLの {source} 部分（例: github）
    @RequestBody String rawBody,     // リクエストの中身（payload、JSON文字列）
    @RequestHeader(name = "X-Signature", required = false) String sig // 署名
) {
    // (1) 署名チェック
    boolean ok = (sig != null) && hmac.verify(rawBody, sig);

    // (2) JSONのidフィールドを取り出して eventId に使う
    String eventId = rawBody.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    if (eventId == null || eventId.isBlank()) eventId = String.valueOf(rawBody.hashCode());

    // (3) すでに保存済みなら無視（同じイベントが2回来ても重複保存しない）
    if (repo.findBySourceAndEventId(source, eventId).isPresent()) {
        return ResponseEntity.ok().build();
    }

    // (4) DBに保存
    InboundEvent e = new InboundEvent();
    e.setSource(source);
    e.setEventId(eventId);
    e.setPayload(rawBody);
    e.setSignatureOk(ok);
    repo.save(e);

    // (5) RabbitMQに送信（後でWorkerが処理する）
    publisher.publish(source, rawBody);

    return ResponseEntity.accepted().build();
}