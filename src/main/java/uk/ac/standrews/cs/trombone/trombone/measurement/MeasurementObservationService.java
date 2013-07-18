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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.standrews.cs.shabdiz.util.Duration;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class MeasurementObservationService {

    private static final int FIRST_FLUSH_DELAY_IN_MILLISECONDS = 0;

    private static final Logger LOGGER = Logger.getLogger(MeasurementObservationService.class.getName());

    @SuppressWarnings("rawtypes")
    private final ConcurrentSkipListMap<Class<? extends MeasurementObserver>, MeasurementObserver> registered_observers;
    private final Timer timer;
    private boolean shut_down;

    /**
     * Instantiates a new measurement observation service.
     *
     * @param flush_interval the flush_interval
     */
    @SuppressWarnings("rawtypes")
    public MeasurementObservationService(final Duration flush_interval) {

        timer = new Timer(true);
        registered_observers = new ConcurrentSkipListMap<Class<? extends MeasurementObserver>, MeasurementObserver>();
        scheduleFlushAtFixedRate(flush_interval);
        shut_down = false;
    }

    /**
     * Register measurement observer.
     *
     * @param observer the observer
     * @return true, if successful
     */
    public boolean registerMeasurementObserver(final MeasurementObserver<?> observer) {

        return !shut_down && registered_observers.putIfAbsent(observer.getClass(), observer) == null;
    }

    /**
     * Gets the measurement observer.
     *
     * @param <T> the generic type
     * @param observer_type the observer_type
     * @return the measurement observer
     */
    @SuppressWarnings("unchecked")
    public <T extends MeasurementObserver<?>> T getMeasurementObserver(final Class<T> observer_type) {

        return (T) registered_observers.get(observer_type);
    }

    /**
     * Shuts down this service. Cancels any further flushing and clears all the registered {@link MeasurementObserver}s.
     * Anterior to the execution of this method {@link #registerMeasurementObserver(MeasurementObserver)} will always return  {@code false};
     */
    public synchronized void shutdown() {

        shut_down = true;
        timer.cancel();
        registered_observers.clear();
    }

    private void scheduleFlushAtFixedRate(final Duration flush_interval) {

        timer.scheduleAtFixedRate(new MeasurementObserverFlushTimerTask(), FIRST_FLUSH_DELAY_IN_MILLISECONDS, flush_interval.getLength(TimeUnit.MILLISECONDS));
    }

    private final class MeasurementObserverFlushTimerTask extends TimerTask {

        @Override
        public void run() {

            for (final MeasurementObserver<?> observer : registered_observers.values()) {
                try {
                    observer.flush();
                }
                catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "unable to flush measurements observed by " + observer, e);
                }
            }
        }
    }
}
