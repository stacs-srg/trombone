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
package uk.ac.standrews.cs.trombone.workload;

import java.util.Random;
import javax.inject.Provider;
import org.mashti.sina.distribution.ProbabilityDistribution;
import org.mashti.sina.util.NumericalRangeValidator;
import org.mashti.sina.util.RandomNumberGenerator;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * Presents a synthetic workload.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ConstantRateWorkload implements Workload {

    private final Provider<Key> key_factory;
    private final ProbabilityDistribution intervals_distribution;
    private final int retry_threshold;
    private final Random uniform_random;

    public ConstantRateWorkload(final ProbabilityDistribution intervals_distribution, final Provider<Key> key_factory, final int retry_threshold, final long seed) {

        NumericalRangeValidator.validateRangeLargerThanOneInclusive(retry_threshold);
        this.intervals_distribution = intervals_distribution;
        this.key_factory = key_factory;
        this.retry_threshold = retry_threshold;
        uniform_random = new Random(seed);
    }

    @Override
    public Lookup getLookupAt(final long time) {

        final long interval = getNextInterval();
        final Key target = getNextTarget();
        return new Lookup(interval, target, retry_threshold);
    }

    private long getNextInterval() {

        synchronized (intervals_distribution) {
            return RandomNumberGenerator.generate(intervals_distribution, uniform_random).longValue();
        }
    }

    private Key getNextTarget() {

        synchronized (key_factory) {
            return key_factory.get();
        }
    }
}
