package uk.ac.standrews.cs.trombone.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ProbabilityDistribution;
import org.mashti.sina.util.RandomNumberGenerator;
import uk.ac.standrews.cs.shabdiz.util.Duration;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class DurationUtil {

    public static Duration generateDurationInNanoseconds(final ProbabilityDistribution distribution, final Random uniform_random) {

        final Number random_number = RandomNumberGenerator.generate(distribution, uniform_random);
        return new Duration(random_number.longValue(), TimeUnit.NANOSECONDS);
    }
}
