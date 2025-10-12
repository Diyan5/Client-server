package registrationApp;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class MainITest {

    @Test
    void start_onEphemeralPort_servesIndex_and_canStop() throws Exception {
        HttpServer s = Main.start(0);
        try {
            int port = s.getAddress().getPort();
            var client = HttpClient.newHttpClient();

            var req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/"))
                    .GET().build();
            var res = client.send(req, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, res.statusCode());
            assertTrue(res.body().toLowerCase().contains("<html"));
        } finally {
            s.stop(0);
        }
    }

    @Test
    void start_onBusyPort_throwsBindException() throws Exception {
        // Резервираме порт, за да симулираме "зает порт"
        HttpServer taken = HttpServer.create(new InetSocketAddress(0), 0);
        taken.start();
        int busyPort = taken.getAddress().getPort();
        try {
            Exception ex = assertThrows(Exception.class, () -> Main.start(busyPort));
            // Причината трябва да е BindException (директно или вложена)
            boolean isBind = ex instanceof java.net.BindException
                    || (ex.getCause() != null && ex.getCause() instanceof java.net.BindException);
            assertTrue(isBind, "Expected BindException as cause or direct");
        } finally {
            taken.stop(0);
        }
    }
}
