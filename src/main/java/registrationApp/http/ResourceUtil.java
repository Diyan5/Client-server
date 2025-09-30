package registrationApp.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceUtil {
    public static byte[] readBytes(String classpathPath) throws IOException {
        if (classpathPath.startsWith("/")) classpathPath = classpathPath.substring(1);
        try (InputStream is = ResourceUtil.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) throw new IOException("Resource not found: " + classpathPath);
            return is.readAllBytes();
        }
    }

    public static String readText(String classpathPath) throws IOException {
        return new String(readBytes(classpathPath), StandardCharsets.UTF_8);
    }
}
