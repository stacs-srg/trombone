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
package uk.ac.standrews.cs.trombone.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements a {@link Timeoutable} that is {@link Runnable} and provides utility methods to notify its listener(s) of its completion.
 * 
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public abstract class TimeoutableRunnable extends TimeoutSupport implements Runnable {

    private static final String DONE_PROPERTY_NAME = "done";
    private final PropertyChangeSupport property_change_support;
    private final AtomicBoolean done;

    protected TimeoutableRunnable() {

        property_change_support = new PropertyChangeSupport(this);
        done = new AtomicBoolean();
    }

    protected void done() {

        if (done.compareAndSet(false, true)) {
            property_change_support.firePropertyChange(DONE_PROPERTY_NAME, false, true);
        }
    }

    protected boolean isDone() {

        return done.get();
    }

    /**
     * Adds the given listener to the collection of listeners to be notified upon completion of this task.
     *
     * @param listener the listener to be notified upon the completion of this task
     */
    public void addCompletionListener(final PropertyChangeListener listener) {

        property_change_support.addPropertyChangeListener(DONE_PROPERTY_NAME, listener);
    }
}
