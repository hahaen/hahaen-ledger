package com.hahaen.ledger.item;

import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.Executors;

/** 本机隔离账号页面验收桥接，仅测试源码；会话只存内存，不输出或保存。 */
final class ItemUiVerifier {
    private ItemUiVerifier() {}
    static void run(int backendPort, String session) throws Exception {
        Path base = Path.of(System.getProperty("basedir")).toAbsolutePath();
        Path root = base.resolve("../app/dist/build/h5").normalize();
        Path signal = base.resolve("target/item-ui-complete");
        Files.deleteIfExists(signal);
        String configured = "http://127.0.0.1:8080";
        Path env = base.resolve("../app/env/.env.production");
        if (Files.exists(env)) {
            for (String line : Files.readAllLines(env)) {
                if (line.startsWith("VITE_API_BASE_URL=")) configured = line.substring(line.indexOf('=') + 1).trim().replaceAll("^['\"]|['\"]$", "").replaceAll("/+$", "");
            }
        }
        String apiBase = configured;
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 18762), 0);
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(executor);
        server.createContext("/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                byte[] body;
                String contentType;
                int status = 200;
                if (path.startsWith("/api/")) {
                    var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + backendPort + exchange.getRequestURI()))
                        .timeout(Duration.ofSeconds(30)).header("X-Auth-Token", session).header("Content-Type", "application/json");
                    byte[] payload = exchange.getRequestBody().readAllBytes();
                    builder.method(exchange.getRequestMethod(), payload.length == 0 ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofByteArray(payload));
                    var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
                    status = response.statusCode(); body = response.body(); contentType = "application/json";
                } else {
                    String normalized = path.replaceFirst("^/haji(?=/)", "");
                    Path file = root.resolve(normalized.equals("/") ? "index.html" : normalized.substring(1)).normalize();
                    if (!file.startsWith(root)) { exchange.sendResponseHeaders(403, -1); return; }
                    body = Files.readAllBytes(file);
                    String name = file.getFileName().toString();
                    contentType = name.endsWith(".js") ? "text/javascript" : name.endsWith(".css") ? "text/css" : name.endsWith(".html") ? "text/html" : name.endsWith(".png") ? "image/png" : name.endsWith(".webp") ? "image/webp" : "application/octet-stream";
                    if (name.endsWith(".js")) body = new String(body, StandardCharsets.UTF_8).replace(apiBase, "http://127.0.0.1:18762").getBytes(StandardCharsets.UTF_8);
                    if (name.endsWith(".html")) body = new String(body, StandardCharsets.UTF_8).replace("<head>", "<head><script>localStorage.setItem('auth-token','" + session + "');</script>").getBytes(StandardCharsets.UTF_8);
                }
                exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
                exchange.getResponseHeaders().set("Cache-Control", "no-store");
                exchange.sendResponseHeaders(status, body.length); exchange.getResponseBody().write(body);
            } catch (Exception error) {
                exchange.sendResponseHeaders(500, -1);
            } finally { exchange.close(); }
        });
        try {
            server.start();
            System.out.println("ITEM_UI_READY http://127.0.0.1:18762/#/pages/items/items (isolated DEV account)");
            long deadline = System.nanoTime() + Duration.ofMinutes(10).toNanos();
            while (!Files.exists(signal) && System.nanoTime() < deadline) Thread.sleep(500);
            if (!Files.exists(signal)) throw new IllegalStateException("UI verification completion was not signaled");
        } finally { server.stop(0); executor.close(); Files.deleteIfExists(signal); }
    }
}
