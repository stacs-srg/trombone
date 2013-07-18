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

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import uk.ac.standrews.cs.shabdiz.util.Duration;

@MappedSuperclass
abstract class PersistableMeasurement {

    protected static final String MEASURABLE_ASPECT_PARAMETER_NAME = "measurable_aspect";
    @Basic(optional = false)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    protected MeasurableAspect measurable_aspect;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;
    @Basic(optional = false)
    @Column(nullable = false, updatable = false)
    private Timestamp digest_timestamp;

    protected PersistableMeasurement() {

    }

    protected PersistableMeasurement(final MeasurableAspect measurable_aspect) {

        digest_timestamp = new Timestamp(System.currentTimeMillis());
        this.measurable_aspect = measurable_aspect;
    }

    public Timestamp getDigestTimestamp() {

        return digest_timestamp;
    }

    public Duration getDigestTimestampDifference(final Timestamp other) {

        final long time_difference = digest_timestamp.getTime() - other.getTime();
        return new Duration(time_difference, TimeUnit.MILLISECONDS);
    }

    long getId() {

        return id;
    }

    void setId(final long id) {

        this.id = id;
    }

    protected void setMeasurableAspect(final MeasurableAspect measurable_aspect) {

        this.measurable_aspect = measurable_aspect;
    }


}
