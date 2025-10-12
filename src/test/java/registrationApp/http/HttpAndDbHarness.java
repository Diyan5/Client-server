package registrationApp.http;

abstract class HttpAndDbHarness {
    protected static com.sun.net.httpserver.HttpServer server;
    protected static int port;

    @org.junit.jupiter.api.BeforeAll
    static void start() throws Exception {
        // ако имаш DB harness – извикай инициализацията там
        server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        registrationApp.http.Router.mount(server);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        port = server.getAddress().getPort();
        System.out.println("TEST HTTP on http://127.0.0.1:" + port);
    }

    @org.junit.jupiter.api.AfterAll
    static void stop() {
        if (server != null) server.stop(0);
    }

    protected String url(String path) {
        if (!path.startsWith("/")) path = "/" + path;
        return "http://127.0.0.1:" + port + path;
    }

    @org.junit.jupiter.api.AfterEach
    void cleanDb() throws Exception {
        // ако имаш TestDbHarness.cleanTables(); извикай го тук
    }
}
