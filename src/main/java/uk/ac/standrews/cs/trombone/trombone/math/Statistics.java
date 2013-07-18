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
package uk.ac.standrews.cs.trombone.trombone.math;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

import static uk.ac.standrews.cs.trombone.trombone.math.ProbabilityDistribution.ONE;
import static uk.ac.standrews.cs.trombone.trombone.math.ProbabilityDistribution.ONE_HUNDRED;
import static uk.ac.standrews.cs.trombone.trombone.math.ProbabilityDistribution.ZERO;

public class Statistics extends StatisticsStateless {

    private final ConcurrentSkipListSet<Sample> sorted_samples;

    public Statistics() {

        sorted_samples = new ConcurrentSkipListSet<Sample>(); // TODO optimise
    }

    @Override
    public void addSample(final Number value) {

        super.addSample(value);
        sorted_samples.add(new Sample(value));
    }

    @Override
    public synchronized void reset() {

        super.reset();
        sorted_samples.clear();
    }

    public Number getPercentile(final double p) {

        NumericalRangeValidator.validateRange(p, ZERO, ONE_HUNDRED, true, true);
        final Number percentile;
        if (sorted_samples.isEmpty()) {
            percentile = Double.NaN;
        }
        else {
            // see http://en.wikipedia.org/wiki/Percentile#Alternative_methods
            final Double sample_size = getSampleSize().doubleValue();
            final Double rank = p * (sample_size - 1) / ONE_HUNDRED + 1;

            if (rank == ONE) {
                percentile = sorted_samples.first().value;
            }
            else if (rank.equals(sample_size)) {
                percentile = sorted_samples.last().value;
            }
            else {

                final long k = (long) Math.floor(rank);
                final double d = rank - k;
                final double k_th_sample = iThSample(k);
                final double k_plus_1_th_sample = iThSample(k + 1);

                percentile = k_th_sample + d * (k_plus_1_th_sample - k_th_sample);
            }
        }
        return percentile;
    }

    private double iThSample(final long i) {

        if (i > 0 && i <= getSampleSize()) {
            long count = 1;
            for (final Sample sample : sorted_samples) {
                if (count == i) { return sample.value; }
                count++;
            }
        }
        return Double.NaN;
    }

    @Override
    public String toString() {

        return "Statistics [getMax()=" + getMax() + ", getMin()=" + getMin() + ", getMean()=" + getMean() + ", getSampleSize()=" + getSampleSize() + "]";
    }

    private static final class Sample implements Comparable<Sample> {

        private static final AtomicLong SAMPLE_ID_GENERATOR = new AtomicLong();
        private final Long id; // To resolve tie when sorting equal values
        private final double value;

        private Sample(final Number value) {

            id = SAMPLE_ID_GENERATOR.incrementAndGet();
            this.value = value.doubleValue();
        }

        @Override
        public int compareTo(final Sample o) {

            assert o != null;
            return equals(o) ? 0 : value == o.value ? id.compareTo(o.id) : Double.compare(value, o.value);
        }

        @Override
        public int hashCode() {

            return id.intValue();
        }

        @Override
        public boolean equals(final Object other) {

            if (other != null && other instanceof Sample) {
                final Sample other_sample = (Sample) other;
                return id.equals(other_sample.id) && value == other_sample.value;
            }
            return false;
        }
    }

}
