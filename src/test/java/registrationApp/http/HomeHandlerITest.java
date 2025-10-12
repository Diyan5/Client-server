package registrationApp.http;

import org.junit.jupiter.api.Test;
import registrationApp.security.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class HomeHandlerITest extends TestHttpServerHarness {

    @Test
    void post_is405() throws Exception {
        var res = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url("/home")))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(405, res.statusCode());
    }

    @Test
    void get_withoutSession_redirectsToLogin() throws Exception {
        var res = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url("/home"))).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        assertEquals(303, res.statusCode());
        assertEquals("/login", res.headers().firstValue("Location").orElse(""));
    }

    @Test
    void get_withValidSession_rendersTemplate_andEscapesName() throws Exception {
        String name = "Diyan <b>&\"' test";
        String sid = SessionManager.create(1L, name, "d@ex.com");

        var req = HttpRequest.newBuilder(URI.create(url("/home")))
                .GET()
                .header("Cookie", "SESSIONID=" + sid)
                .build();
        var res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        String body = res.body();
        assertTrue(body.contains("Welcome"));
        // Проверяваме, че HTML е ескейпнат (няма сурови <, >, & и т.н.)
        assertFalse(body.contains("<b>"));      // не трябва да се интерпретира като HTML
        assertTrue(body.contains("Diyan &lt;b&gt;&amp;&quot;&#39; test"));
    }
}
