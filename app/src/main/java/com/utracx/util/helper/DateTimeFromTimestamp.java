package com.utracx.util.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeFromTimestamp {

    String dateTime;
    String date;
    String time;
    String yesterday;
    String fullMonthDate;
    String yesterdayFullMonthDate;
    long SECONDS_IN_A_DAY = 86400;

    public DateTimeFromTimestamp(long timestamp) {
        this.dateTime = formatter.format(new Date(timestamp*1000));
        this.date = dateFormatter.format(new Date(timestamp*1000));
        this.time = timeFormatter.format(new Date(timestamp*1000));
        this.yesterday = dateFormatter.format(new Date((timestamp-SECONDS_IN_A_DAY)*1000));
        this.fullMonthDate = fullMonthDateFormatter.format(new Date(timestamp*1000));
        this.yesterdayFullMonthDate = fullMonthDateFormatter.format(new Date((timestamp-SECONDS_IN_A_DAY)*1000));
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getYesterday() { return yesterday; }

    public String getFullMonthDate() {
        return fullMonthDate;
    }

    public String getYesterdayFullMonthDate() {
        return yesterdayFullMonthDate;
    }

    static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
    static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    static SimpleDateFormat fullMonthDateFormatter = new SimpleDateFormat("dd MMMM yyyy");
    static SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");

}
