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
package uk.ac.standrews.cs.trombone.trombone.measurement.persistence;

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * A factory for creating {@link JPAPersistor} objects.
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class JPAPersistorFactory {

    protected final EntityManagerFactory entity_manager_factory;

    public JPAPersistorFactory(final String persistence_unit_name, final Properties persistence_properties) {

        this(Persistence.createEntityManagerFactory(persistence_unit_name, persistence_properties));
    }

    public JPAPersistorFactory(final EntityManagerFactory entity_manager_factory) {

        this.entity_manager_factory = entity_manager_factory;
    }

    /**
     * Creates a new Instance of {@link JPAPersistor}.
     *
     * @return a new Instance of {@link JPAPersistor}
     */
    public JPAPersistor newPersistor() {

        return new JPAPersistor(entity_manager_factory.createEntityManager());
    }
}
