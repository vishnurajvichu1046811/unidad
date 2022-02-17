package com.utracx.util.helper;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {

    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss aa", Locale.ENGLISH);

    private DateTimeHelper() {
    }

    public static String getDisplayableDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    public static boolean isSameDay(@NonNull Date date1, @NonNull Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isSameDay(@NonNull Long date1, @NonNull Long date2) {

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(date2);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }

    public static long getStartTimeOfDay(long timeInMillis) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(timeInMillis);
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        return calendar1.getTimeInMillis();
    }

    public static long getEndTimeOfDay(long timeInMillis) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(timeInMillis);
        calendar1.set(Calendar.HOUR_OF_DAY, 23);
        calendar1.set(Calendar.MINUTE, 59);
        calendar1.set(Calendar.SECOND, 59);
        calendar1.set(Calendar.MILLISECOND, 999);
        return calendar1.getTimeInMillis();
    }

    public static long getUpdatedDate(long timeInMillis, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static String getDateRange(long startTimeInMills, long endTimeInMills) {
        return String.format("%s - %s", new SimpleDateFormat("hh:mm a",
                        Locale.getDefault()).format(startTimeInMills),
                new SimpleDateFormat("hh:mm a",
                        Locale.getDefault()).format(endTimeInMills)).toUpperCase();
    }
}
