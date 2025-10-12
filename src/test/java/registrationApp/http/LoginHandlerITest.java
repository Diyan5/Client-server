package registrationApp.http;

import org.junit.jupiter.api.Test;
import registrationApp.db.UsersDao;
import registrationApp.security.PasswordHasher;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class LoginHandlerITest extends TestHttpServerHarness {

    @Test
    void getLogin_returnsForm() throws Exception {
        var res = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url("/login"))).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertTrue(res.body().toLowerCase().contains("<form"));
    }

    @Test
    void postLogin_invalid_showsError() throws Exception {
        var res = post("/login", form("email","no@such.com", "password","bad"));
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Invalid email or password"));
    }

    @Test
    void postLogin_success_setsCookie_andRedirects() throws Exception {
        // Подготвяме user в DB
        var dao = new UsersDao();
        String email = "john@example.com";
        String pass  = "Aa123456";
        byte[] salt = PasswordHasher.newSalt();
        byte[] hash = PasswordHasher.hash(pass.toCharArray(), salt);
        dao.insert("John", email, hash, salt);

        var res = post("/login", form("email", email, "password", pass));
        assertEquals(303, res.statusCode());
        assertEquals("/home", location(res));

        var setCookie = res.headers().firstValue("Set-Cookie").orElse("");
        assertTrue(setCookie.contains("SESSIONID="));
        assertTrue(setCookie.contains("HttpOnly"));
    }

    // helpers
    private static String form(String... kv) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            if (b.length() > 0) b.append('&');
            b.append(URLEncoder.encode(kv[i], StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(kv[i+1], StandardCharsets.UTF_8));
        }
        return b.toString();
    }
    private static HttpResponse<String> post(String path, String body) throws Exception {
        var req = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
    }
    private static String location(HttpResponse<?> res) { return res.headers().firstValue("Location").orElse(""); }
}
