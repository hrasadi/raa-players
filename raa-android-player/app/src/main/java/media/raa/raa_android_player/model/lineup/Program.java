package media.raa.raa_android_player.model.lineup;

/**
 * Represents one program in the lineup list
 * Created by hamid on 9/30/17.
 */

@SuppressWarnings("WeakerAccess")
public class Program {
    public final String programTime;
    public final String programName;
    public final String programClips;

    public Program(String programTime, String programName, String programClips) {
        this.programTime = programTime;
        this.programName = programName;
        this.programClips = programClips;
    }

    @Override
    public String toString() {
        return programName;
    }
}