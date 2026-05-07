package lynx.team2.rest_api;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LUtils {
    public static String longToIsoDate(long date) {
        return Instant.ofEpochSecond(date).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static long isoToEpochSecond(String iso) {
        return LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toEpochSecond(ZoneOffset.UTC);
    }
}
