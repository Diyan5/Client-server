package registrationApp.http;

import org.junit.jupiter.api.Test;
import registrationApp.db.UsersDao;
import registrationApp.db.TestDbHarness;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterHandlerITest extends TestHttpServerHarness {
    // Marker интерфейс за да „взема“ @BeforeAll/@AfterEach от TestDbHarness чрез композиция – виж по-долу

    @Test
    void getRegister_returnsForm() throws Exception {
        var res = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url("/register"))).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());
        assertTrue(res.body().toLowerCase().contains("<form"));
    }

    @Test
    void postRegister_invalidEmail_showsError() throws Exception {
        var form = form("name", "Diyan", "email", "bad-email", "password", "Aa123456");
        var res = post("/register", form);
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("Invalid email"));
    }

    @Test
    void postRegister_duplicateEmail_showsError() throws Exception {
        var dao = new UsersDao();
        var okForm = form("name","Alice", "email","alice@example.com","password","Aa123456");
        // първа успешна регистрация
        var res1 = post("/register", okForm);
        assertEquals(303, res1.statusCode());

        // втора с дублиран email
        var res2 = post("/register", okForm);
        assertEquals(200, res2.statusCode());
        assertTrue(res2.body().contains("already registered"));
    }

    @Test
    void postRegister_success_redirectsToLogin() throws Exception {
        var form = form("name","Bob", "email","bob@example.com","password","Aa123456");
        var res = post("/register", form);
        assertEquals(303, res.statusCode());
        assertEquals("/login?fromReg=1", location(res));
    }

    // ------------ helpers ------------
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

    private static String location(HttpResponse<?> res) {
        return res.headers().firstValue("Location").orElse("");
    }
}
