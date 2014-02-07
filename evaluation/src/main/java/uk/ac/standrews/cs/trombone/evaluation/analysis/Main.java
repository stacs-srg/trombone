package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Main {

    public static void main(String[] args) throws IOException {

        List<ScenarioAnalyzer> scenarioAnalyzers = new ArrayList<>();
        scenarioAnalyzers.add(new ScenarioAnalyzer("PlatformJustificationMultipleHost"));
        scenarioAnalyzers.add(new ScenarioAnalyzer("PlatformJustificationSingleHost"));
        for (ScenarioAnalyzer scenarioAnalyzer : scenarioAnalyzers) {

            try (ScenarioAnalyzer scenario_analyzer = scenarioAnalyzer) {

                new AvailablePeerCountAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new EventExecutionDurationTimerAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new EventExecutionLagAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new JoinFailureRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new JoinSuccessRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupCorrectnessDelayAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupCorrectnessHopCountAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupCorrectnessRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupCorrectnessRetryCountAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupExecutionRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupFailureRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupIncorrectnessDelayAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupIncorrectnessHopCountAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupIncorrectnessRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new LookupIncorrectnessRetryCountAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new PeerArrivalRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new PeerDepartureRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
                new SentBytesRateAnalyzer(scenario_analyzer).saveAsSVG(scenario_analyzer.getAnalysisDirectory().toFile());
            }
        }
    }
}
