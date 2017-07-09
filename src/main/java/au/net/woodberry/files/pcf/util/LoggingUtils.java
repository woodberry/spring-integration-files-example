package au.net.woodberry.files.pcf.util;

import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;

public class LoggingUtils {

    private LoggingUtils() {
        // Do not instantiate a utils class.
    }

    public static String throwableToMessage(Throwable ex, Object... args) {
        return String.format("An exception of type: \"%s\" has occurred with message: \"%s\". Caused by: \"%s\". Input args: %s",
                ex.getClass().getCanonicalName(),
                ex.getMessage() == null ? "No message" : ex.getMessage(),
                ex.getCause() == null ? "No cause" : ex.getCause().toString(),
                args == null ? "No input arguments" : Strings.join(Arrays.asList(args), ',')
        );
    }

    public static String throwableToMessage(Throwable ex) {
        return throwableToMessage(ex, null);
    }
}
