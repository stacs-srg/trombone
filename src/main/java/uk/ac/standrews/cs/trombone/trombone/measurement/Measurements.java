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
package uk.ac.standrews.cs.trombone.trombone.measurement;

import com.codahale.metrics.Clock;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.UniformReservoir;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.trombone.Peer;

public final class Measurements extends  MetricRegistry{

    private static final String PEER_STATE_SIZE_METRIC_NAME = "PEER_STATE_SIZE";
    private static final Histogram PEER_STATE_SIZE_HISTOGRAM = new Histogram(new UniformReservoir());
    private static final String PEER_ARRIVAL_RATE_METRIC_NAME = "PEER_ARRIVAL_RATE";
    private static final String PEER_DEPARTURE_RATE_METRIC_NAME = "PEER_DEPARTURE_RATE";
    private static final String SENT_BITS_RATE_PER_PEER_METRIC_NAME = "SENT_BITS_RATE";
    private final MetricRegistry metric_registry;
    private final ScheduledReporter reporter;



    private Measurements(final MetricRegistry metric_registry) {

        this.metric_registry = metric_registry;
        metric_registry.register(PEER_STATE_SIZE_METRIC_NAME, PEER_STATE_SIZE_HISTOGRAM);

        reporter = CsvReporter.forRegistry(metric_registry).build(new File(""));
    }

    public static Metric getMetric(MeasurableAspect a) {

        return null;
    }

    public void registerPeer(Peer peer) {
    }


}
