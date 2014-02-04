package uk.ac.standrews.cs.trombone.evaluation.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class ZipFileSystemUtils {

    private ZipFileSystemUtils() {

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
}
