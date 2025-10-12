package registrationApp.http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutHandlerITest extends TestHttpServerHarness {

    @Test
    void logout_expiresCookie_andRedirects() throws Exception {
        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create(url("/logout")))
                .GET()
                .header("Cookie","SESSIONID=abc123")
                .build();
        var res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(303, res.statusCode());
        assertEquals("/login?lo=1", res.headers().firstValue("Location").orElse(""));

        var cookies = res.headers().allValues("Set-Cookie");
        assertTrue(cookies.stream().anyMatch(c -> c.startsWith("SESSIONID=") && c.contains("Max-Age=0")));
        assertTrue(cookies.stream().anyMatch(c -> c.startsWith("JUST_LOGGED_OUT=1")));
    }
}
