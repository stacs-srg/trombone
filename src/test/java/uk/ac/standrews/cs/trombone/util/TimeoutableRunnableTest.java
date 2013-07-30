package uk.ac.standrews.cs.trombone.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class TimeoutableRunnableTest {

    private TimeoutableRunnable timeoutable_runnable;
    private boolean notified;

    @Before
    public void setUp() throws Exception {

        timeoutable_runnable = new TimeoutableRunnable() {

            @Override
            public void run() {

                done();
            }
        };

        timeoutable_runnable.addCompletionListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {

                notified = true;
            }
        });
    }

    @Test
    public void testCompletionNotification() throws Exception {

        timeoutable_runnable.run();
        Assert.assertTrue(notified);
    }
}
