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
package uk.ac.standrews.cs.trombone.evaluation.provider;

import java.util.Random;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.churn.ConstantRateUncorrelatedChurn;

public class ConstantRateUncorrelatedUniformChurnProvider implements SerializableProvider<Churn> {

    private final ProbabilityDistribution session_length_distribution;
    private final ProbabilityDistribution downtime_distribution;
    private final SerializableProvider<Long> seed_provider;
    private final Random uniform_random;

    public ConstantRateUncorrelatedUniformChurnProvider(final ProbabilityDistribution session_length_distribution, final ProbabilityDistribution downtime_distribution, final SerializableProvider<Long> seed_provider) {

        this.session_length_distribution = session_length_distribution;
        this.downtime_distribution = downtime_distribution;
        this.seed_provider = seed_provider;
        uniform_random = new Random(seed_provider.get());
    }

    @Override
    public synchronized Churn get() {

        final Long seed = seed_provider.get();
        return new ConstantRateUncorrelatedChurn(session_length_distribution, downtime_distribution, seed);
    }
}
