package uk.ac.standrews.cs.trombone.metric.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MetricRegistry {

    private final ConcurrentSkipListMap<String, Metric> metrics;
    private final String name;

    public MetricRegistry(String name) {

        this.name = name;

        metrics = new ConcurrentSkipListMap<String, Metric>();
    }

    public boolean register(String name, Metric metric) {

        return metrics.putIfAbsent(name, metric) == null;
    }

    public Set<Map.Entry<String, Metric>> getMetrics() {

        return new CopyOnWriteArraySet<Map.Entry<String, Metric>>(metrics.entrySet());
    }

    public String getName() {

        return name;
    }
}
