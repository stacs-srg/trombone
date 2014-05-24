package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ScenarioAnalyzer implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioAnalyzer.class);
    private final String scenario_name;
    public static final PathMatcher ZIP_FILE_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**/*.zip");
    private final List<Path> repetitions;
    private final List<FileSystem> file_systems;

    public ScenarioAnalyzer(String scenario_name) throws IOException {

        this.scenario_name = scenario_name;
        repetitions = getRepetitions();

        // TODO unshard only if the data is sharded
        LOGGER.info("unsharding {}", scenario_name);
        AnalyticsUtil.unshard(repetitions, true);

        file_systems = new ArrayList<>();
        for (Path repetition : repetitions) {
            file_systems.add(FileSystemUtils.newZipFileSystem(repetition, false));
        }
    }

    public List<Path> getCsvsByName(String glob_filter) throws IOException {

        final List<Path> csvs = new ArrayList<>();

        for (FileSystem file_system : file_systems) {
            final PathMatcher matcher = file_system.getPathMatcher("glob:" + glob_filter);
            final List<Path> matched_files = FileSystemUtils.getMatchingFiles(file_system.getPath(file_system.getSeparator()), matcher);
            csvs.addAll(matched_files);
        }

        return csvs;
    }

    @Override
    public void close() throws IOException {

        for (FileSystem file_system : file_systems) {
            file_system.close();
        }
    }

    public Path getAnalysisDirectory() throws IOException {

        final Path path = Paths.get("results", scenario_name, "analysis");
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    public String getScenarioName() {

        return scenario_name;
    }

    private List<Path> getRepetitions() throws IOException {

        final Path repetitions_home = getRepetitionsHomeDirectory(scenario_name);
        return FileSystemUtils.getMatchingFiles(repetitions_home, ZIP_FILE_MATCHER);
    }

    private static Path getRepetitionsHomeDirectory(final String scenario_name) {

        return Paths.get("results", scenario_name, "repetitions");
    }
}
