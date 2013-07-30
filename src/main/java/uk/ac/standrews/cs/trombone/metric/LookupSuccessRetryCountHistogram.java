package uk.ac.standrews.cs.trombone.metric;

import uk.ac.standrews.cs.trombone.math.Statistics;
import uk.ac.standrews.cs.trombone.metric.core.Sampler;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupSuccessRetryCountHistogram extends Sampler {

    private static final Sampler GLOBAL = new Sampler();
    Statistics statistics = new Statistics();

    public static Sampler getGlobal() {

        return GLOBAL;
    }

    @Override
    public void update(final long value) {

        try {
            super.update(value);
        }
        finally {
            GLOBAL.update(value);
        }
    }
}
