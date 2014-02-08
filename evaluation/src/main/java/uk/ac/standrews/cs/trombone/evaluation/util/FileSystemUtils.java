package uk.ac.standrews.cs.trombone.evaluation.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class FileSystemUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemUtils.class);

    private FileSystemUtils() {

    }

    public static List<Path> getMatchingFiles(final Path path, final PathMatcher matcher) throws IOException {

        final List<Path> matched_files = new ArrayList<>();
        Files.walkFileTree(path, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {

                if (matcher.matches(file)) {
                    matched_files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {

                throw exc;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {

                return FileVisitResult.CONTINUE;
            }
        });
        return matched_files;
    }

    public static FileSystem newZipFileSystem(File zip_file, boolean create) throws IOException {

        return newZipFileSystem(zip_file.getAbsolutePath(), create);
    }

    public static FileSystem newZipFileSystem(Path path_to_zip, boolean create) throws IOException {

        return newZipFileSystem(path_to_zip.toAbsolutePath().toString(), create);
    }

    public static FileSystem newZipFileSystem(String path_to_zip, boolean create) throws IOException {

        final File zip = new File(path_to_zip);
        final URI zip_uri = URI.create("jar:" + zip.toURI().toString());
        final Map<String, String> environment = new HashMap<>();
        environment.put("create", String.valueOf(create));
        return FileSystems.newFileSystem(zip_uri, environment);
    }

    public static void copyRecursively(final Path source_directory, final Path destination_directory) throws IOException {

        final FileVisitor<Path> visitor = new FileVisitor<Path>() {

            Path current = destination_directory;

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {

                final Path fileName = dir.getFileName();
                if (fileName != null) {
                    current = current.resolve(fileName);
                    if (!Files.isDirectory(current)) {
                        Files.copy(dir, current, StandardCopyOption.COPY_ATTRIBUTES);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path source, final BasicFileAttributes attrs) throws IOException {

                final Path target = current.resolve(source.getFileName());
                if (!Files.isRegularFile(target)) {
                    Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exception) throws IOException {

                LOGGER.error("failed to copy file: " + file, exception);
                throw exception;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {

                if (dir != null) {
                    current = current.getParent();
                }
                return FileVisitResult.CONTINUE;
            }

        };
        Files.walkFileTree(source_directory, visitor);
    }
}
