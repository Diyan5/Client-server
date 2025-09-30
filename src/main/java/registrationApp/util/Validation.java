package registrationApp.util;

import java.util.regex.Pattern;


public final class Validation {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern NAME =
            Pattern.compile("^[A-Za-zА-Яа-я'\\- ]{2,100}$");

    private static final Pattern PW_LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern PW_UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern PW_DIGIT = Pattern.compile(".*[0-9].*");

    public static boolean isEmail(String s) {
        return s != null && s.length() <= 255 && EMAIL.matcher(s).matches();
    }

    public static boolean isName(String s) {
        return s != null && NAME.matcher(s.trim()).matches();
    }

    public static boolean isStrongPassword(String s) {
        return s != null
                && s.length() >= 8
                && PW_LOWER.matcher(s).matches()
                && PW_UPPER.matcher(s).matches()
                && PW_DIGIT.matcher(s).matches();
    }
}
