package uk.ac.standrews.cs.trombone.metric;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.metric.core.Timer;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupSuccessDelayTimer extends Timer {

    private static final Timer GLOBAL = new Timer();

    public static Timer getGlobal() {

        return GLOBAL;
    }

    @Override
    public void update(final long duration, final TimeUnit unit) {

        try {
            super.update(duration, unit);
        }
        finally {
            GLOBAL.update(duration, unit);
        }
    }
}
