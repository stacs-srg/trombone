package uk.ac.standrews.cs.trombone.evaluation.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class ScenarioUtils {

    private static final Path RESULTS_HOME = Paths.get("results");

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

    public static Path getScenarioEventsPath(String scenario_name) {

        return getScenarioHome(scenario_name).resolve("events.zip");
    }
}
