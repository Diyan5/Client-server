package registrationApp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class TemplateHandler implements HttpHandler {
    private final String templatePath;

    public TemplateHandler(String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendHtml(ex, 405, "<h1>405 Method Not Allowed</h1>");
            return;
        }
        try {
            String html = ResourceUtil.readText(templatePath);
            HttpUtil.sendHtml(ex, 200, html);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendHtml(ex, 500, "<h1>500 Internal Server Error</h1>");
        }
    }
}
