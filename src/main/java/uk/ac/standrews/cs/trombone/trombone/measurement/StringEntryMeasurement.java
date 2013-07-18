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
import java.util.Map.Entry;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "STRING_ENTRY_MEASUREMENTS", uniqueConstraints = @UniqueConstraint(columnNames = {"key", "measurable_aspect"}))
@Access(AccessType.FIELD)
@NamedQuery(name = StringEntryMeasurement.FIND_BY_MEASURABLE_ASPECT_QUERY_NAME, query = "SELECT measurement FROM StringEntryMeasurement measurement WHERE measurement.measurable_aspect = :" + PersistableMeasurement.MEASURABLE_ASPECT_PARAMETER_NAME)
public class StringEntryMeasurement extends PersistableMeasurement implements Entry<String, String> {

    protected static final String FIND_BY_MEASURABLE_ASPECT_QUERY_NAME = "findStringEntryMeasurementByMeasurableAspect";
    private String key;
    @Lob
    @Column(length = 512)
    private String value;

    /** No-args constructor required by JPA. */
    protected StringEntryMeasurement() {

    }

    public StringEntryMeasurement(final String key, final Object value) {

        this(key, String.valueOf(value));
    }

    public StringEntryMeasurement(final String key, final String value) {

        this.key = key;
        this.value = value;
    }

    public static List<StringEntryMeasurement> findByMeasurableAspect(final EntityManager entity_manager, final MeasurableAspect aspect) {

        final TypedQuery<StringEntryMeasurement> query = entity_manager.createNamedQuery(FIND_BY_MEASURABLE_ASPECT_QUERY_NAME, StringEntryMeasurement.class);
        query.setParameter(MEASURABLE_ASPECT_PARAMETER_NAME, aspect);
        return query.getResultList();
    }

    @Override
    public String getKey() {

        return key;
    }

    @Override
    public String getValue() {

        return value;
    }

    @Override
    public synchronized String setValue(final String value) {

        final String old_value = this.value;
        this.value = value;
        return old_value;
    }
}
