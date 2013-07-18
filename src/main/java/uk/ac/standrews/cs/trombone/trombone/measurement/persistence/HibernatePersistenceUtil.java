/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Trombone.
 *
 * Trombone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trombone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Trombone.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.trombone.trombone.measurement.persistence;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import org.h2.tools.Backup;
import org.h2.tools.DeleteDbFiles;

public final class HibernatePersistenceUtil {

    private static final String DEFAULT_H2_PASSWORD = "";
    private static final String DEFAULT_H2_USERNAME = "sa";
    private static final String OPTIONS = ";DEFRAG_ALWAYS=TRUE";
    private static final String ZIP_FILE_EXTENSION = ".zip";
    private static final String JDBC_H2_URL_PREFIX = "jdbc:h2:";
    static {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
    }

    private HibernatePersistenceUtil() {

    }

    public static Properties constructH2DatabaseProperties(final File db_file, final String db_name, final boolean create_db) {

        return constructH2DatabaseProperties(db_file, db_name, DEFAULT_H2_USERNAME, DEFAULT_H2_PASSWORD, create_db, false);
    }

    public static Properties constructH2DatabaseProperties(final File db_file, final String db_name, final String username, final String password, final boolean create_db, final boolean debug) {

        final Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("javax.persistence.jdbc.user", username);
        properties.put("javax.persistence.jdbc.password", password);
        properties.put("javax.persistence.jdbc.url", constructH2JdbcUrlFromFile(db_file, db_name));
        properties.put("javax.persistence.sharedCache.mode", "none");

        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.cache.use_second_level_cache", "false");
        properties.put("hibernate.jdbc.batch_size", "50");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.current_session_context_class", "thread");

        if (create_db) {
            properties.put("hibernate.hbm2ddl.auto", "create");
        }

        if (debug) {
            properties.put("hibernate.generate_statistics", "true");
            properties.put("hibernate.use_sql_comments", "true");
            properties.put("hibernate.show_sql", "true");
        }
        return properties;

    }

    public static File exportH2DBToZipFile(final String h2_db_name, final File source_directory, final File destination, final boolean delete_db_files) throws SQLException {

        final String source_dicetory_path = source_directory.getAbsolutePath();
        Backup.execute(destination.getAbsolutePath(), source_dicetory_path, h2_db_name, false);

        if (delete_db_files) {
            DeleteDbFiles.execute(source_dicetory_path, h2_db_name, false);
        }

        return destination;
    }

    private static String constructH2JdbcUrlFromFile(final File db_file, final String db_name) {

        final String db_file_path = db_file.getAbsolutePath();
        final StringBuffer h2_jdbc_url = new StringBuffer();

        h2_jdbc_url.append(JDBC_H2_URL_PREFIX);
        if (isZippedFile(db_file)) {
            h2_jdbc_url.append("zip:");
            h2_jdbc_url.append(db_file_path);
            h2_jdbc_url.append("!/");
        }
        else {
            h2_jdbc_url.append("file:");
            h2_jdbc_url.append(db_file_path);
            h2_jdbc_url.append(File.separator);
        }
        h2_jdbc_url.append(db_name);
        h2_jdbc_url.append(OPTIONS);
        return h2_jdbc_url.toString();
    }

    private static boolean isZippedFile(final File file) {

        return !file.isDirectory() && file.getName().endsWith(ZIP_FILE_EXTENSION);
    }
}
