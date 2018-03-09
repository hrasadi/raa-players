package media.raa.raa_android_player.model.entities.archive;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import media.raa.raa_android_player.model.JSONReader;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;

/**
 * Directory of archived programs. Also contains utilities to obtain archive list for one program
 * Created by hamid on 3/7/18.
 */
public class Archive extends JSONReader {
    private static final String ARCHIVE_BASE_URL = RaaContext.BASE_URL + "/archive";
    private static final String ARCHIVE_DIRECTORY_URL = ARCHIVE_BASE_URL + "/raa1-archive.json";

    private Map<String, String> archiveDirectory = new HashMap<>();
    private Map<String, List<ArchiveEntry>> programArchivesCache = new HashMap<>();

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    @Override
    public Promise reload() {
        return dm.when(() -> this.readRemoteJSON(ARCHIVE_DIRECTORY_URL))
                .done(this::parseArchiveDirectory);
    }

    @SuppressWarnings("unchecked")
    public Promise loadProgramArchive(String programId) {
        if (!archiveDirectory.containsKey(programId)) {
            return new DeferredObject().reject(programId);
        }

        String programArchiveUrl = ARCHIVE_BASE_URL + '/' + archiveDirectory.get(programId);
        return dm.when(() -> this.readRemoteJSON(programArchiveUrl))
                .done((rs) -> programArchivesCache.put(programId, this.parseProgramArchive(rs)));
    }

    private List<ArchiveEntry> parseProgramArchive(String serverResponse) {
        try {
            if (serverResponse != null) {
                Map<String, List<Program>> programArchive = gson.fromJson(serverResponse,
                        new TypeToken<Map<String, List<Program>>>() {
                        }.getType());

                return this.flattenProgramArchive(programArchive);
            }
        } catch (JsonSyntaxException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
        return null;
    }

    private List<ArchiveEntry> flattenProgramArchive(Map<String, List<Program>> programArchive) {
        List<ArchiveEntry> flatProgramArchive = new ArrayList<>();
        List<String> dates = new ArrayList<>(programArchive.keySet());
        Collections.sort(dates);
        Collections.reverse(dates);
        for (String date: dates) {
            for (Program p : programArchive.get(date)) {
                ArchiveEntry entry = new ArchiveEntry(p, date);
                flatProgramArchive.add(entry);
            }
        }
        return flatProgramArchive;
    }

    public List<ArchiveEntry> getProgramArchive(String programId) {
        if (this.programArchivesCache.containsKey(programId)) {
            return this.programArchivesCache.get(programId);
        }
        return null;
    }

    private void parseArchiveDirectory(String serverResponse) {
        try {
            if (serverResponse != null) {
                this.archiveDirectory = gson.fromJson(serverResponse,
                        new TypeToken<Map<String, String>>() {
                        }.getType());
            }
        } catch (JsonSyntaxException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
    }

    /**
     * Result of this function call should be used along with ProgramInfoDirectory items.
     * @return list of programIds for which we have archive entries
     */
    public List<String> getArchiveDirectoryProgramIds() {
        List<String> archivePrograms = new ArrayList<>();
        archivePrograms.addAll(this.archiveDirectory.keySet());
        return archivePrograms;
    }
}
