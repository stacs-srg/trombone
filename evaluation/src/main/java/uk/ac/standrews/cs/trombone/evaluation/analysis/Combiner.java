package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Combiner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Combiner.class);

    public static void unshard(Collection<Path> raw_zip_files) throws IOException {

        for (Path raw_zip_file : raw_zip_files) {
            try (FileSystem fileSystem = FileSystemUtils.newZipFileSystem(raw_zip_file, false)) {

                final Path host_1 = fileSystem.getPath("1");
                final List<Path> csv_list = FileSystemUtils.getMatchingFiles(host_1, fileSystem.getPathMatcher("glob:/1/*.csv"));

                for (Path path : csv_list) {
                    final Path csv_file = path.getFileName();
                    final List<Path> all_csvs = FileSystemUtils.getMatchingFiles(fileSystem.getPath(fileSystem.getSeparator()), fileSystem.getPathMatcher("glob:/[0-9]*/" + csv_file));

                    final String csv_file_name = path.toString();
                    if (csv_file_name.contains("_counter") || csv_file_name.contains("_gauge") || csv_file_name.contains("_size")) {

                        AnalyticsUtil.unshardCountCsv(all_csvs, Files.newBufferedWriter(csv_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                    }
                    else if (csv_file_name.contains("_rate")) {

                        AnalyticsUtil.unshardRateCsv(all_csvs, Files.newBufferedWriter(csv_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                    }
                    else if (csv_file_name.contains("_sampler")) {

                        AnalyticsUtil.unshardSamplerCsv(all_csvs, Files.newBufferedWriter(csv_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                    }
                    else if (csv_file_name.contains("_timer")) {

                        AnalyticsUtil.unshardTimerCsv(all_csvs, Files.newBufferedWriter(csv_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                    }
                    else {
                        LOGGER.warn("unknown csv type : {}. Skipped.", csv_file);
                    }
                }

            }
        }
    }
}
