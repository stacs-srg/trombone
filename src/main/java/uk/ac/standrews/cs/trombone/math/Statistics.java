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
package uk.ac.standrews.cs.trombone.math;

import java.util.Collections;
import java.util.List;

import static uk.ac.standrews.cs.trombone.math.ProbabilityDistribution.ONE_HUNDRED;
import static uk.ac.standrews.cs.trombone.math.ProbabilityDistribution.ZERO;

public class Statistics extends StatisticsStateless {

    private final UniformReservoir reservoir;

    public Statistics() {

        reservoir = new UniformReservoir();
    }

    @Override
    public void addSample(final Number value) {

        super.addSample(value);
        reservoir.update(value);
    }

    @Override
    public synchronized void reset() {

        super.reset();
        reservoir.reset();
    }

    @Override
    public String toString() {

        return "Statistics [getMax()=" + getMax() + ", getMin()=" + getMin() + ", getMean()=" + getMean() + ", getSampleSize()=" + getSampleSize() + "]";
    }

    public Number getPercentile(final double quantile) {

        NumericalRangeValidator.validateRange(quantile, ZERO, ONE_HUNDRED, true, true);
        final Number percentile;

        final List<Double> snapshot = reservoir.getSnapshot();
        if (snapshot.isEmpty()) {
            percentile = Double.NaN;
        }
        else {
            Collections.sort(snapshot); //FIXME optimize
            final int sample_size = snapshot.size();
            final double position = quantile / 100 * (sample_size + 1);

            if (position < 1) {
                percentile = snapshot.get(0);
            }
            else if (position >= sample_size) {
                percentile = snapshot.get(sample_size - 1);
            }
            else {

                final double lower = snapshot.get((int) position - 1);
                final double upper = snapshot.get((int) position);
                percentile = lower + (position - Math.floor(position)) * (upper - lower);
            }
        }

        return percentile;
    }
}
