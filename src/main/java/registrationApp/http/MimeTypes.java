package registrationApp.http;

import java.util.Map;

public final class MimeTypes {

    private static final Map<String,String> MAP = Map.ofEntries(
            Map.entry(".html","text/html; charset=UTF-8"),
            Map.entry(".css","text/css; charset=UTF-8"),
            Map.entry(".js","application/javascript; charset=UTF-8"),
            Map.entry(".png","image/png")
    );

    public static String of(String path) {
        int dot = path.lastIndexOf('.');
        String ext = dot >= 0 ? path.substring(dot).toLowerCase() : "";
        return MAP.getOrDefault(ext, "application/octet-stream");
    }
}
