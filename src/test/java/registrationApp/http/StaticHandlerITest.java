package registrationApp.http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class StaticHandlerITest extends TestHttpServerHarness {

    @Test
    void servesCssWithContentType() throws Exception {
        var client = HttpClient.newHttpClient();
        // избери реален CSS от resources/static/css (пример: styles.css)
        var req = HttpRequest.newBuilder(URI.create(url("/css/styles.css"))).GET().build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

        // ако файлът не съществува – смени името спрямо проекта ти
        assertEquals(200, res.statusCode());
        assertEquals("text/css; charset=UTF-8", res.headers().firstValue("Content-Type").orElse(""));
        assertTrue(res.body().length() > 0);
    }

    @Test
    void nonExisting_static_returns404() throws Exception {
        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create(url("/css/__missing__.css"))).GET().build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, res.statusCode());
    }
}
