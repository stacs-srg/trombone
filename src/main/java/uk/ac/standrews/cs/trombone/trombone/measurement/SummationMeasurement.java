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

import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

@Entity
@Table(name = "SUMMATION_MEASUREMENTS")
@Access(AccessType.FIELD)
@NamedQuery(name = SummationMeasurement.FIND_BY_MEASURABLE_ASPECT_QUERY_NAME, query = "SELECT measurement FROM SummationMeasurement measurement WHERE measurement.measurable_aspect = :" + PersistableMeasurement.MEASURABLE_ASPECT_PARAMETER_NAME)
public class SummationMeasurement extends PersistableMeasurement {

    protected static final String FIND_BY_MEASURABLE_ASPECT_QUERY_NAME = "findSummationMeasurementByMeasurableAspect";
    private long summation;

    protected SummationMeasurement() {

    }

    public SummationMeasurement(final long summation, final MeasurableAspect digest_type) {

        super(digest_type);
        this.summation = summation;
    }

    public static List<SummationMeasurement> findByMeasurableAspect(final EntityManager entity_manager, final MeasurableAspect aspect) {

        final TypedQuery<SummationMeasurement> query = entity_manager.createNamedQuery(FIND_BY_MEASURABLE_ASPECT_QUERY_NAME, SummationMeasurement.class);
        query.setParameter(MEASURABLE_ASPECT_PARAMETER_NAME, aspect);
        return query.getResultList();
    }

    public long getSummation() {

        return summation;
    }
}
