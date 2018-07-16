import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TimeSplit {

    private static int section;

    public static void setSection(int section) {
        TimeSplit.section = section;
    }

    public static List<String> timeSplit(String start, String end) {
        List<String> timeSlice = new ArrayList<>();
        LocalDateTime sTime = stringToLocalDateTime(start);
        LocalDateTime eTime = stringToLocalDateTime(end);
        if (eTime != null) {
            if (sTime != null) {
                while (eTime.isAfter(sTime)) {
                    sTime = sTime.plus(section, ChronoUnit.SECONDS);
                    if (sTime.isAfter(eTime))
                        sTime = eTime;
                    timeSlice.add(sTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
            }
        }
        return timeSlice;
    }

    public static LocalDateTime stringToLocalDateTime(String time){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date date = sdf.parse(time);
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getDifference(String start, String end){
        return Duration.between(Objects.requireNonNull(stringToLocalDateTime(start)), stringToLocalDateTime(end)).toSeconds();
    }

    public static Boolean compare(String time1, String time2){
        LocalDateTime localDateTime1 = stringToLocalDateTime(time1);
        LocalDateTime localDateTime2 = stringToLocalDateTime(time2);
        if (localDateTime2 != null) {
            if (localDateTime1 != null) {
                return localDateTime1.isBefore(localDateTime2) || localDateTime1.isEqual(localDateTime2);
            }
        }
        return null;
    }
}
