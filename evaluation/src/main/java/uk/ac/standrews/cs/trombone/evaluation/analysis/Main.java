package uk.ac.standrews.cs.trombone.evaluation.analysis;

import com.google.visualization.datasource.base.TypeMismatchException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Main {

    public static void main(String[] args) throws IOException, TypeMismatchException {

        List<ScenarioAnalyzer> scenarioAnalyzers = new ArrayList<>();
                                                           
        for (int i = 1; i <= 140; i++) {
            scenarioAnalyzers.add(new ScenarioAnalyzer("scenario_" + i));
        }

        for (ScenarioAnalyzer scenarioAnalyzer : scenarioAnalyzers) {
            try (ScenarioAnalyzer scenario_analyzer = scenarioAnalyzer) {

                final File destination_directory = scenario_analyzer.getAnalysisDirectory().toFile();

                new AvailablePeerCountAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new EventExecutionDurationTimerAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new EventExecutionLagAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new JoinFailureRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new JoinSuccessRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupCorrectnessDelayAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupCorrectnessHopCountAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupCorrectnessRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupCorrectnessRetryCountAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupExecutionRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupFailureRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupIncorrectnessDelayAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupIncorrectnessHopCountAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupIncorrectnessRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new LookupIncorrectnessRetryCountAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new PeerArrivalRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new PeerDepartureRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new SentBytesRateAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new ReachableStateSizePerPeerAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new SystemLoadAverageAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new ThreadCountAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
                new ThreadCpuUsageAnalyzer(scenario_analyzer).saveAsSVG(destination_directory).saveAsJson(destination_directory).saveAsCsv(destination_directory);
            }
        }
    }
}
