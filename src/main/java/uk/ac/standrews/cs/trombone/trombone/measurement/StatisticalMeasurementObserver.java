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

import java.util.concurrent.atomic.AtomicReference;
import uk.ac.standrews.cs.trombone.trombone.math.Statistics;
import uk.ac.standrews.cs.trombone.trombone.measurement.persistence.Persistor;

public class StatisticalMeasurementObserver<Value extends Number> implements MeasurementObserver<Value> {

    private final Persistor persistor;
    private final AtomicReference<Statistics> statistics_reference;
    private final MeasurableAspect digest_type;

    protected StatisticalMeasurementObserver(final Persistor persistor, final MeasurableAspect digest_type) {

        this.persistor = persistor;
        this.digest_type = digest_type;
        statistics_reference = new AtomicReference<Statistics>();
        refreshStatistics();
    }

    @Override
    public boolean isObservable(final Value value) {

        return value != null;
    }

    @Override
    public void notify(final Value value) {

        if (isObservable(value)) {
            statistics_reference.get().addSample(value);
        }
    }

    @Override
    public void flush() {

        final Statistics statistics = refreshStatistics();
        final StatisticalMeasurement digest = new StatisticalMeasurement(statistics, digest_type);
        statistics.reset();
        persistor.persist(digest);
    }

    private Statistics refreshStatistics() {

        return statistics_reference.getAndSet(new Statistics());
    }
}
