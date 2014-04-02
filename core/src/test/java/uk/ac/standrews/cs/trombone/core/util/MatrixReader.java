package uk.ac.standrews.cs.trombone.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class MatrixReader {

    private static final Pattern MATRIX_FILE_DELIMITER = Pattern.compile("\\s+|\\t+");

    private MatrixReader() {

    }

    public static RealMatrix toMatrix(Path file_path) throws IOException {

        return new Array2DRowRealMatrix(readMatrix(file_path));
    }

    private static double[][] readMatrix(final Path file_path) throws IOException {

        try (final BufferedReader reader = Files.newBufferedReader(file_path, StandardCharsets.UTF_8)) {

            final List<double[]> data_list = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {

                final String[] textual_data = MATRIX_FILE_DELIMITER.split(line.trim());
                final double[] data_row = new double[textual_data.length];

                for (int j = 0; j < textual_data.length; j++) {
                    String value = textual_data[j];
                    data_row[j] = Double.valueOf(value);
                }

                data_list.add(data_row);
            }

            final double[][] data = new double[data_list.size()][];
            for (int i = 0; i < data.length; i++) {
                data[i] = data_list.get(i);
            }
            return data;
        }
    }
}
