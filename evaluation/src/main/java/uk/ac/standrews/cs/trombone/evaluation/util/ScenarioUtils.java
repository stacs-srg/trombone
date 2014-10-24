package uk.ac.standrews.cs.trombone.evaluation.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class ScenarioUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioUtils.class);
    private static final Path RESULTS_HOME = Paths.get("results");

    public static final JsonFactory JSON_FACTORY = new JsonFactory();
    public  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(JSON_FACTORY);

    static {
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.registerModule(new TromboneEvaluationModule());
    }

    private ScenarioUtils() {

    }

    public static Path getScenarioHome(Scenario scenario) {

        return getScenarioHome(scenario.getName());
    }

    public static Path getResultsHome() {

        return RESULTS_HOME;
    }

    public static Path getScenarioHome(String scenario_name) {

        return RESULTS_HOME.resolve(scenario_name);
    }

    public static Path getScenarioRepetitionsHome(String scenario_name) {

        return getScenarioHome(scenario_name).resolve("repetitions");
    }

    public static Path getScenarioAnalysisHome(String scenario_name) {

        return getScenarioHome(scenario_name).resolve("analysis");
    }

    public static List<Path> getRepetitions(String scenario_name) throws IOException {

        final Path repetitionsHome = getScenarioRepetitionsHome(scenario_name);
        return FileSystemUtils.getMatchingFiles(repetitionsHome, FileSystems.getDefault()
                .getPathMatcher("glob:**/*.zip"));

    }

    public static void saveScenarioAsJson(Scenario scenario, Path directory) throws IOException {

        final String scenario_json = OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(scenario);
        final Path json_path = directory.resolve("scenario.json");
        Files.write(json_path, scenario_json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public static JSONObject readScenarioAsJson(Path directory) throws IOException {

        final Path json_path = directory.resolve("scenario.json");
        final String scenario_json_string = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Files.readAllBytes(json_path)))
                .toString();
        return new JSONObject(scenario_json_string);
    }

    public static void compressDirectoryRecursively(Path directory, Path destination) throws Exception {

        try (final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination.toFile()))) {
            LOGGER.info("Compressing {}", directory);
            compressPath(directory, out);
        }
    }

    public static List<FileSystem> getRepetitionsFileSystems(final String scenario_name) throws IOException {

        return getRepetitions(scenario_name).stream()
                .map(ScenarioUtils:: toZipFileSystem)
                .collect(Collectors.toList());
    }

    private static FileSystem toZipFileSystem(Path path) {

        try {
            return FileSystemUtils.newZipFileSystem(path, false);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compressPath(final Path path, final ZipOutputStream out) throws IOException {

        final byte[] tmpBuf = new byte[8192];
        Files.walkFileTree(path, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) {

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {

                LOGGER.debug(" compressing {}", file);
                out.putNextEntry(new ZipEntry(path.relativize(file)
                        .toString()));

                try (final InputStream in = Files.newInputStream(file, StandardOpenOption.READ)) {
                    int read;
                    while ((read = in.read(tmpBuf)) > 0) {
                        out.write(tmpBuf, 0, read);
                    }
                    out.closeEntry();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) {

                LOGGER.error("failed to compress {}, due to {}", file, exc);
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {

                return FileVisitResult.CONTINUE;
            }
        });
    }
}
