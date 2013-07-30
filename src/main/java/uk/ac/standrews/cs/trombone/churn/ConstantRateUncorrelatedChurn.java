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
package uk.ac.standrews.cs.trombone.churn;

import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.math.ProbabilityDistribution;

/**
 * Implements a churn model where arrivals and departures are not correlated.
 * This churn model is based on the work done by Yao et al. in their paper titled as <a href="http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=4110276">Modeling heterogeneous user churn and local resilience of unstructured P2P networks</a>.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ConstantRateUncorrelatedChurn extends UncorrelatedChurn {

    private final ProbabilityDistribution session_lengths;
    private final ProbabilityDistribution downtimes;

    public ConstantRateUncorrelatedChurn(final Duration first_arrival_delay, final ProbabilityDistribution session_lengths, final ProbabilityDistribution downtimes, final long seed) {

        super(first_arrival_delay, seed);
        this.session_lengths = session_lengths;
        this.downtimes = downtimes;
    }

    @Override
    protected Duration getSessionLengthAt(final long time) {

        return generateRandomDurationFromDistribution(session_lengths);
    }

    @Override
    protected Duration getDowntimeAt(final long time) {

        return generateRandomDurationFromDistribution(downtimes);
    }
}
