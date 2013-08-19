package uk.ac.standrews.cs.trombone.metric.core;

import java.util.concurrent.atomic.AtomicReference;
import org.mashti.sina.distribution.statistic.Statistics;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Sampler implements Metric {

    private final AtomicReference<Statistics> statistics;

    public Sampler() {

        statistics = new AtomicReference<Statistics>(new Statistics());
    }

    public Statistics getAndReset() {

        return statistics.getAndSet(new Statistics());
    }

    public void update(long sample) {

        get().addSample(sample);
    }

    protected Statistics get() {

        return statistics.get();
    }
}
