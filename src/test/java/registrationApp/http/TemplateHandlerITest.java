package registrationApp.http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateHandlerITest extends TestHttpServerHarness {

    @Test
    void getRoot_returnsIndexHtml() throws Exception {
        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create(url("/"))).GET().build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertTrue(res.body().toLowerCase().contains("<html"));
    }

    @Test
    void postRoot_isMethodNotAllowed() throws Exception {
        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create(url("/"))).POST(HttpRequest.BodyPublishers.noBody()).build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, res.statusCode());
        assertTrue(res.body().contains("405"));
    }
}
