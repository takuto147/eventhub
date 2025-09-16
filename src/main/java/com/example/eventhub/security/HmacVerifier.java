package com.example.eventhub.security;


import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;


@Component
public class HmacVerifier {
private final String secret;
public HmacVerifier(org.springframework.core.env.Environment env) {
this.secret = env.getProperty("app.hmac-secret", "devsecret");
}
public boolean verify(String body, String signatureHex) {
try {
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
StringBuilder sb = new StringBuilder();
for (byte b : hash) sb.append(String.format("%02x", b));
return sb.toString().equalsIgnoreCase(signatureHex);
} catch (Exception e) {
return false;
}
}
}