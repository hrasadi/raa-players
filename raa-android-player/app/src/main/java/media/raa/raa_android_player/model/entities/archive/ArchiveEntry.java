package media.raa.raa_android_player.model.entities.archive;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import media.raa.raa_android_player.Utils;
import media.raa.raa_android_player.model.entities.Program;

/**
 * Represents one program in archive
 * Created by hamid on 3/7/18.
 */

public class ArchiveEntry {
    private Program program;
    private String releaseDate;

    ArchiveEntry(Program program, String releaseDate) {
        this.program = program;
        this.releaseDate = releaseDate;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    private String getReleaseDate() {
        return releaseDate;
    }

    public String getFormattedReleaseDate() {
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("YYYY-MM-dd");
        DateTime d = dateTimeFormat.parseDateTime(getReleaseDate());

        String dateString = Integer.toString(d.getDayOfMonth()) + " " +
                Utils.getMonthStringInPersianLocale(d.getMonthOfYear()) + " "  +
                Integer.toString(d.getYear());

        return Utils.convertToPersianLocaleString(dateString);
    }

}
