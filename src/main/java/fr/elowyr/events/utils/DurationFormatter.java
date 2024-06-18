package fr.elowyr.events.utils;

import org.apache.commons.lang.time.DurationFormatUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DurationFormatter {
    private static long oneMinute;
    private static long oneHour;
    private static long oneDay;
    public static SimpleDateFormat frenchDateFormat;
    public static ThreadLocal<DecimalFormat> remainingSeconds;
    public static ThreadLocal<DecimalFormat> remainingSecondsTrailing;

    public DurationFormatter() {
    }

    public static String getRemaining(long millis, boolean milliseconds) {
        return getRemaining(millis, milliseconds, true);
    }

    public static String getRemaining(long duration, boolean milliseconds, boolean trail) {
        if (milliseconds && duration < oneMinute) {
            return ((trail ? remainingSecondsTrailing : remainingSeconds).get()).format((double)duration * 0.001D) + 's';
        } else {
            return duration >= oneDay ? DurationFormatUtils.formatDuration(duration, "dd-HH:mm:ss") : DurationFormatUtils.formatDuration(duration, (duration >= oneHour ? "HH:" : "") + "mm:ss");
        }
    }

    public static String getDurationWords(long duration) {
        return DurationFormatUtils.formatDuration(duration, "d' jours 'H' heures 'm' minutes 's' secondes'");
    }

    public static String getDurationDate(long duration) {
        return frenchDateFormat.format(new Date(duration));
    }

    public static String getCurrentDate() {
        return frenchDateFormat.format(new Date());
    }

    static {
        oneMinute = TimeUnit.MINUTES.toMillis(1L);
        oneHour = TimeUnit.HOURS.toMillis(1L);
        oneDay = TimeUnit.DAYS.toMillis(1L);
        frenchDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        remainingSeconds = new ThreadLocal<DecimalFormat>() {
            protected DecimalFormat initialValue() {
                return new DecimalFormat("0.#");
            }
        };
        remainingSecondsTrailing = new ThreadLocal<DecimalFormat>() {
            protected DecimalFormat initialValue() {
                return new DecimalFormat("0.0");
            }
        };
    }
}
