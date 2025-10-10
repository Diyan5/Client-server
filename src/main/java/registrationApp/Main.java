package registrationApp;

import com.sun.net.httpserver.HttpServer;
import registrationApp.http.Router;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    /** Стартира сървър на подадения порт (0 = епхемерален). Връща инстанцията за контрол в тестове. */
    static HttpServer start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        Router.mount(server);
        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("Server started on http://localhost:" + server.getAddress().getPort());
        server.start();
        return server;
    }

    public static void main(String[] args) throws Exception {
        start(8080);
    }
}
