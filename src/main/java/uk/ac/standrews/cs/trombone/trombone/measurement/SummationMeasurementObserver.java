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

import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import uk.ac.standrews.cs.trombone.trombone.measurement.persistence.Persistor;

@Entity
@Table(name = "SUMMATION_MEASUREMENTS")
@Access(AccessType.FIELD)
@NamedQuery(name = SummationMeasurement.FIND_BY_MEASURABLE_ASPECT_QUERY_NAME, query = "SELECT measurement FROM SummationMeasurement measurement WHERE measurement.measurable_aspect = :" + PersistableMeasurement.MEASURABLE_ASPECT_PARAMETER_NAME)
public class SummationMeasurementObserver implements MeasurementObserver<Long> {

    private final AtomicLong summation;
    private final Persistor persistor;
    private final MeasurableAspect digest_type;

    SummationMeasurementObserver(final Persistor persistor, final MeasurableAspect digest_type) {

        this.persistor = persistor;
        this.digest_type = digest_type;
        summation = new AtomicLong();
    }

    @Override
    public boolean isObservable(final Long value) {

        return Long.class.isInstance(value) && value >= 0;
    }

    @Override
    public void notify(final Long value) {

        if (isObservable(value)) {
            summation.addAndGet(value);
        }
    }

    public void increment() {

        notify(1L);
    }

    @Override
    public void flush() {

        final long current_summation = summation.getAndSet(0);
        final SummationMeasurement summation_digest = new SummationMeasurement(current_summation, digest_type);
        persistor.persist(summation_digest);
    }
}
