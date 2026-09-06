package com.hahaen.ledger.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CaptchaService {
    private static final String PREFIX = "haji:auth:captcha:";
    private static final int TTL_SECONDS = 300;
    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final DefaultRedisScript<String> GET_AND_DELETE = new DefaultRedisScript<>(
            "local v = redis.call('get', KEYS[1]); if v then redis.call('del', KEYS[1]); return v; end; return nil",
            String.class);

    private final StringRedisTemplate redis;

    public CaptchaService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public Captcha create() {
        String id = UUID.randomUUID().toString().replace("-", "");
        String answer = randomText(4);
        redis.opsForValue().set(PREFIX + id, answer, Duration.ofSeconds(TTL_SECONDS));
        return new Captcha(id, svgDataUrl(answer), TTL_SECONDS);
    }

    public boolean consume(String id, String input) {
        if (id == null || id.isBlank() || input == null || input.isBlank()) return false;
        String answer = redis.execute(GET_AND_DELETE, List.of(PREFIX + id));
        return answer != null && answer.equalsIgnoreCase(input.trim());
    }

    private static String randomText(int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(CHARACTERS.charAt(ThreadLocalRandom.current().nextInt(CHARACTERS.length())));
        }
        return result.toString();
    }

    private static String svgDataUrl(String answer) {
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='240' height='88' viewBox='0 0 240 88'>"
                + "<rect width='240' height='88' rx='12' fill='#f5fcfa'/>"
                + "<path d='M8 22 C70 8 150 80 232 24 M4 70 C84 32 130 42 235 68' stroke='#b7e1d9' stroke-width='3' fill='none'/>"
                + "<text x='120' y='58' text-anchor='middle' font-family='Georgia,serif' font-size='36' font-weight='700' font-style='italic' letter-spacing='10' fill='#278879'>"
                + answer + "</text></svg>";
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    public record Captcha(String id, String image, int expiresInSeconds) {
    }
}
