package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class Analysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(Analysis.class);
    private static final Pattern UNDERSCORE = Pattern.compile("_");

    public static void main(String[] args) throws IOException {

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:results/*");
        final List<Path> matching_paths = FileSystemUtils.getMatchingDirectories(ScenarioUtils.getResultsHome(), matcher);
        final List<ScenarioAnalyzer> scenarioAnalyzers = new ArrayList<>();
        for (Path path : matching_paths) {
            updateRepetitionsList(path);
            final String scenario_name = path.getFileName().toString();
            final ScenarioAnalyzer analyzer = new ScenarioAnalyzer(scenario_name);
            scenarioAnalyzers.add(analyzer);
        }

        Set<String> unique_file_names = null;
        for (ScenarioAnalyzer scenarioAnalyzer : scenarioAnalyzers) {
            try (ScenarioAnalyzer scenario_analyzer = scenarioAnalyzer) {

                final File destination_directory = scenario_analyzer.getAnalysisDirectory().toFile();
                if (unique_file_names == null) {
                    unique_file_names = new HashSet<>();
                    final List<Path> csvs = scenario_analyzer.getCsvsByName("/*.csv");
                    for (Path csv : csvs) {
                        unique_file_names.add(csv.getFileName().toString());
                    }
                }

                for (String file_name : unique_file_names) {

                    XYCsvAnalyzer analyser = null;
                    final String base_name = FilenameUtils.getBaseName(file_name);
                    final List<Path> files = scenario_analyzer.getCsvsByName("/" + file_name);

                    if (file_name.endsWith("counter.csv") || file_name.endsWith("size.csv") || file_name.endsWith("gauge.csv")) {
                        analyser = new XYCsvAnalyzer.Counter(base_name, files);
                    }
                    if (file_name.endsWith("timer.csv")) {
                        analyser = new XYCsvAnalyzer.Timer(base_name, files);
                    }

                    if (file_name.endsWith("sampler.csv")) {
                        analyser = new XYCsvAnalyzer.Sampler(base_name, files);
                    }
                    if (file_name.endsWith("rate.csv")) {
                        analyser = new XYCsvAnalyzer.Rate(base_name, files);
                    }

                    if (analyser == null) {
                        LOGGER.warn("skipped {}, unknown file name pattern in scenario {}", file_name, scenario_analyzer.getScenarioName());
                    }
                    else {

                        throw new RuntimeException("IMPLEMENT");

                        //                        analyser.setYAxisLabel(UNDERSCORE.matcher(base_name).replaceAll(" "));
                        //                        analyser.saveAsCsv(destination_directory);
                    }
                }
            }
        }
    }

    private static void updateRepetitionsList(final Path path) throws IOException {

        final JSONArray repetitions_path = new JSONArray();
        final List<Path> repetitions = FileSystemUtils.getMatchingFiles(path, path.getFileSystem().getPathMatcher("glob:**/repetitions/*.zip"));
        for (Path repetition : repetitions) {
            repetitions_path.put(repetition.getFileName().toString());
        }

        Files.write(path.resolve("repetitions/repetitions.json"), repetitions_path.toString(4).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
    }
}
