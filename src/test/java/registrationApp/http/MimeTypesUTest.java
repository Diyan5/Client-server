package registrationApp.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MimeTypesUTest {

    @Test
    void returnsKnownTypesOrOctetStream() {
        assertEquals("text/html; charset=UTF-8", MimeTypes.of("index.html"));
        assertEquals("text/css; charset=UTF-8",  MimeTypes.of("styles.css"));
        assertEquals("application/javascript; charset=UTF-8", MimeTypes.of("app.js"));
        assertEquals("image/png",               MimeTypes.of("logo.png"));
        assertEquals("application/octet-stream", MimeTypes.of("file.unknown"));
        assertEquals("application/octet-stream", MimeTypes.of("noext"));
    }
}
