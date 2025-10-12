package registrationApp.http;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceUtilUTest {

    @Test
    void readText_existingResource_ok() throws IOException {
        // избери ресурс, който със сигурност го имаш (примерно templates/index.html)
        String html = ResourceUtil.readText("templates/index.html");
        assertNotNull(html);
        assertTrue(html.length() > 0);
    }

    @Test
    void readBytes_missingResource_throws() {
        IOException ex = assertThrows(IOException.class,
                () -> ResourceUtil.readBytes("no/such/file.txt"));
        assertTrue(ex.getMessage().contains("Resource not found"));
    }
}
