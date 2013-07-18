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

import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.io.File;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.trombone.measurement.persistence.Persistor;

public class PeerStateSizeMeasurementObserver extends StatisticalMeasurementObserver<Integer> {

    protected PeerStateSizeMeasurementObserver(final Persistor persistor) {

        super(persistor, MeasurableAspect.PEER_STATE_SIZE);
    }

    public static void main(String[] args) {

        MetricRegistry registry = new MetricRegistry();
        final Histogram sss = registry.histogram("histogram");
        final Counter counter = registry.counter("counter");

        final Meter meter = registry.meter("meter");
        final Timer timer = registry.timer("timer");

        final CsvReporter reporter = CsvReporter.forRegistry(registry).build(new File("/Users/masih/Desktop"));
        final Random random = new Random();

        registry.register("gauge", new Gauge<Integer>() {

            @Override
            public Integer getValue() {

                return random.nextInt();
            }
        });
        reporter.start(1, TimeUnit.SECONDS);
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {

                final Timer.Context time = timer.time();
                sss.update(random.nextInt());
                counter.inc();
                meter.mark();
                time.stop();

            }
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isObservable(final Integer value) {

        return super.isObservable(value) && value >= 0L;
    }
}
