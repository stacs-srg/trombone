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

import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import uk.ac.standrews.cs.trombone.trombone.math.NumericalRangeValidator;

public class JPAPersistor implements Persistor {

    /** The Constant DEFAULT_BATCH_SIZE. */
    public static final int DEFAULT_BATCH_SIZE = 5;

    private final EntityManager entitiy_manager;
    private final int batch_size;
    private volatile EntityTransaction transaction;
    private volatile int persist_count;

    public JPAPersistor(final EntityManager entitiy_manager) {

        this(entitiy_manager, DEFAULT_BATCH_SIZE);
    }

    public JPAPersistor(final EntityManager entitiy_manager, final int batch_size) {

        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(batch_size);
        this.entitiy_manager = entitiy_manager;
        this.batch_size = batch_size;
        resetTransaction();
    }

    @Override
    public synchronized void persist(final Object entity) {

        entitiy_manager.persist(entity);
        if (isBatchFull()) {
            commitFlushAndClear();
            resetTransaction();
        }
    }

    @Override
    public synchronized void close() throws IOException {

        commitFlushAndClear();
        entitiy_manager.close();
    }

    private boolean isBatchFull() {

        return persist_count++ >= batch_size;
    }

    private void resetTransaction() {

        transaction = entitiy_manager.getTransaction();
        transaction.begin();
        persist_count = 0;
    }

    private void commitFlushAndClear() {

        try {
            if (transaction != null) {
                entitiy_manager.flush();
                entitiy_manager.clear();
                transaction.commit();
            }
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
