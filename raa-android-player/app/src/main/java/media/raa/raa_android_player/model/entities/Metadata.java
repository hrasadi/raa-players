package media.raa.raa_android_player.model.entities;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Locale;

import media.raa.raa_android_player.Utils;

/**
 * A program metadata describes when a program will begin or end
 * Created by hamid on 3/1/18.
 */

@SuppressWarnings("unused")
public class Metadata {
    private String startTime;
    private String endTime;

    private DateTimeFormatter isoFormatter;

    public Metadata() {
        this.isoFormatter = ISODateTimeFormat.dateTime();
    }

    private String getFormattedTime(String time) {
        DateTime d = isoFormatter.parseDateTime(time);
        String zonedDateString = d.withZone(DateTimeZone.getDefault()).toString("HH:mm", Locale.US);

        return Utils.convertToPersianLocaleString(zonedDateString);
    }

    public String getStartTime() {
        return startTime;
    }

    public String getFormattedStartTime() {
        return this.getFormattedTime(startTime);
    }

    public String getStartTimeRelativeDay() {
        return Utils.getRelativeDayName(isoFormatter.parseDateTime(startTime));
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getFormattedEndTime() {
        return this.getFormattedTime(endTime);
    }

    public String getEndTimeRelativeDay() {
        return Utils.getRelativeDayName(isoFormatter.parseDateTime(endTime));
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
