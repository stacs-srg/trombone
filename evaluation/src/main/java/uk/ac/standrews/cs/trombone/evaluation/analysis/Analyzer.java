package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import org.jfree.chart.JFreeChart;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface Analyzer {

    String getName();

    JFreeChart getChart() throws IOException;
}
