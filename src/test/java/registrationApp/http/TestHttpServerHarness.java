package registrationApp.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class TestHttpServerHarness {

    protected static HttpServer server;
    protected static int port;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0); // свободен порт
        Router.mount(server);
        server.start();
        port = server.getAddress().getPort();
        System.out.println("Test HTTP server on port " + port);
    }

    @AfterAll
    static void stopServer() {
        if (server != null) server.stop(0);
    }

    protected static String url(String path) {
        if (!path.startsWith("/")) path = "/" + path;
        return "http://127.0.0.1:" + port + path;
    }
}
