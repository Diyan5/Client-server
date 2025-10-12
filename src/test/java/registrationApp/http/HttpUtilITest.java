package registrationApp.http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpUtilITest extends TestHttpServerHarness {

    @Test
    void readFormUrlEncoded_parsesBody() throws Exception {
        // Тестов echo handler
        server.createContext("/_echo", ex -> {
            var form = HttpUtil.readFormUrlEncoded(ex);
            String out = form.getOrDefault("name","") + "|" + form.getOrDefault("email","");
            HttpUtil.sendHtml(ex, 200, out);
        });

        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create(url("/_echo")))
                .header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("name=Diyan&email=d%40ex.com"))
                .build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertEquals("Diyan|d@ex.com", res.body());
        assertEquals("text/html; charset=UTF-8", res.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void redirectSeeOther_sets303AndLocation() throws Exception {
        server.createContext("/_redir", ex -> HttpUtil.redirectSeeOther(ex, "/target"));

        var res = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url("/_redir"))).GET().build(),
                HttpResponse.BodyHandlers.discarding());

        assertEquals(303, res.statusCode());
        assertEquals("/target", res.headers().firstValue("Location").orElse(""));
    }

    @Test
    void noCache_setsHeaders() throws Exception {
        server.createContext("/_nocache", ex -> {
            HttpUtil.noCache(ex);
            HttpUtil.sendHtml(ex, 200, "ok");
        });

        var res = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url("/_nocache"))).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        var h = res.headers();
        assertTrue(h.firstValue("Cache-Control").orElse("").contains("no-store"));
        assertTrue(h.allValues("Cache-Control").stream().anyMatch(v -> v.contains("pre-check")));
        assertEquals("no-cache", h.firstValue("Pragma").orElse(""));
        assertEquals("0", h.firstValue("Expires").orElse(""));
        assertEquals("Cookie", h.firstValue("Vary").orElse(""));
    }
}
