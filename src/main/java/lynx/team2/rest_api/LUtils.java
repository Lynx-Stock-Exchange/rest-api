package lynx.team2.rest_api;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LUtils {
    public static String longToIsoDate(long date) {
        return Instant.ofEpochSecond(date).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
