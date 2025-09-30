package registrationApp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class StaticHandler implements HttpHandler {
    private final String cpBase;
    private final String urlBase;

    public StaticHandler(String urlBase, String cpBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase : urlBase + "/";
        this.cpBase  = cpBase.endsWith("/")  ? cpBase  : cpBase  + "/";
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            if (!path.startsWith(urlBase)) {
                HttpUtil.sendHtml(ex, 404, "<h1>404</h1>");
                return;
            }
            String file = path.substring(urlBase.length());
            String cp   = cpBase + file;
            byte[] bytes = ResourceUtil.readBytes(cp);
            ex.getResponseHeaders().set("Content-Type", MimeTypes.of(file));
            ex.sendResponseHeaders(200, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.getResponseBody().close();
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendHtml(ex, 404, "<h1>404 Not Found</h1>");
        }
    }
}
