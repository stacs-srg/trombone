package uk.ac.standrews.cs.trombone.metric;

import uk.ac.standrews.cs.trombone.metric.core.Rate;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupFailureRateMeter extends Rate {

    private static final Rate GLOBAL = new Rate();

    public static Rate getGlobal() {

        return GLOBAL;
    }

    @Override
    public void mark(final long n) {

        try {
            super.mark(n);
        }
        finally {
            GLOBAL.mark(n);
        }
    }
}
