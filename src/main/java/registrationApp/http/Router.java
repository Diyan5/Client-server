package registrationApp.http;

import com.sun.net.httpserver.HttpServer;

public final class Router {
    public static void mount(HttpServer s) {
        s.createContext("/css",    new StaticHandler("/css/",    "static/css"));
        s.createContext("/images", new StaticHandler("/images/", "static/images"));
        s.createContext("/",         new TemplateHandler("templates/index.html"));
        s.createContext("/register", new RegisterHandler());
        s.createContext("/login",    new LoginHandler());
        s.createContext("/home",     new HomeHandler());
        s.createContext("/logout",   new LogoutHandler());
        s.createContext("/js", new StaticHandler("/js/", "static/js"));
    }
}
