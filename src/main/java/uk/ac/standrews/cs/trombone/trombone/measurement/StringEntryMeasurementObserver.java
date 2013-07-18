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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import uk.ac.standrews.cs.trombone.trombone.measurement.persistence.Persistor;

/**
 * An asynchronous update interface for receiving notifications about {@link StringEntryMeasurement} information as the {@link StringEntryMeasurement} is constructed.
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class StringEntryMeasurementObserver implements MeasurementObserver<StringEntryMeasurement> {

    private static final long serialVersionUID = 712525300072912308L;
    private final Persistor persistor;
    private final AtomicReference<Set<StringEntryMeasurement>> entries_reference;
    private final MeasurableAspect measurable_aspect;

    /**
     * This method is called when information about an {@link StringEntryMeasurement} which was previously requested using an asynchronous interface becomes available.
     * @param persistor the persistor
     */
    StringEntryMeasurementObserver(final Persistor persistor, final MeasurableAspect measurable_aspect) {

        this.persistor = persistor;
        this.measurable_aspect = measurable_aspect;
        entries_reference = new AtomicReference<Set<StringEntryMeasurement>>();
        refreshReference();
    }

    @Override
    public boolean isObservable(final StringEntryMeasurement measurement) {

        return measurement != null;
    }

    @Override
    public void notify(final StringEntryMeasurement measurement) {

        if (isObservable(measurement)) {
            entries_reference.get().add(measurement);
        }
    }

    public void notify(final String key, final Object value) {

        notify(new StringEntryMeasurement(key, value));
    }

    public void notify(final Map<String, String> measurements) {

        for (final Entry<String, String> entry : measurements.entrySet()) {
            notify(entry.getKey(), entry.getValue());
        }
    }

    public void notify(final Collection<StringEntryMeasurement> measurements) {

        for (final StringEntryMeasurement measurement : measurements) {
            notify(measurement);
        }
    }

    @Override
    public void flush() throws IOException {

        final Set<StringEntryMeasurement> entries = refreshReference();
        for (final StringEntryMeasurement entry : entries) {
            entry.setMeasurableAspect(measurable_aspect);
            persistor.persist(entry);
        }
    }

    private Set<StringEntryMeasurement> refreshReference() {

        return entries_reference.getAndSet(new HashSet<StringEntryMeasurement>());
    }
}
