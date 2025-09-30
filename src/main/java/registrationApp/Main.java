package registrationApp;

import com.sun.net.httpserver.HttpServer;
import registrationApp.http.Router;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        Router.mount(server);
        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("Server started on http://localhost:" + port);
        server.start();
    }
}
