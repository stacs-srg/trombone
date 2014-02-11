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

package uk.ac.standrews.cs.trombone.event.provider;

import javax.inject.Provider;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.trombone.core.util.Repeatable;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.churn.ConstantRateUncorrelatedChurn;

public class ConstantRateUncorrelatedChurnProvider implements Provider<Churn>, Repeatable {

    private final ProbabilityDistribution session_length_distribution;
    private final ProbabilityDistribution downtime_distribution;
    private final RandomSeedProvider seed_provider;

    public ConstantRateUncorrelatedChurnProvider(final ProbabilityDistribution session_length_distribution, final ProbabilityDistribution downtime_distribution, final RandomSeedProvider seed_provider) {

        this.session_length_distribution = session_length_distribution;
        this.downtime_distribution = downtime_distribution;
        this.seed_provider = seed_provider;
    }

    @Override
    public synchronized Churn get() {

        final Long seed = seed_provider.get();
        return new ConstantRateUncorrelatedChurn(session_length_distribution, downtime_distribution, seed);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("ConstantRateUncorrelatedUniformChurnProvider{");
        sb.append("session_length_distribution=").append(session_length_distribution);
        sb.append(", downtime_distribution=").append(downtime_distribution);
        sb.append(", seed_provider=").append(seed_provider);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public synchronized void setSeed(final long seed) {

        seed_provider.setSeed(seed);
    }

    @Override
    public long getSeed() {

        return seed_provider.getSeed();
    }

    @Override
    public ConstantRateUncorrelatedChurnProvider clone()  {

        return new ConstantRateUncorrelatedChurnProvider(session_length_distribution, downtime_distribution, seed_provider.clone());
    }
}
