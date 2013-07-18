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
package uk.ac.standrews.cs.trombone.trombone.churn;

import com.google.inject.Provider;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;
import uk.ac.standrews.cs.trombone.trombone.math.ProbabilityDistribution;
import uk.ac.standrews.cs.trombone.trombone.math.RandomNumberGenerator;
import uk.ac.standrews.cs.trombone.trombone.util.SeedProvider;
import uk.ac.standrews.cs.shabdiz.util.Duration;

@Singleton
public class ConstantRateUncorrelatedUniformChurnProvider implements Provider<Churn> {

    private final ProbabilityDistribution first_arrival_delay_distribution;
    private final ProbabilityDistribution session_length_distribution;
    private final ProbabilityDistribution downtime_distribution;
    private final SeedProvider seed_provider;
    private final Random uniform_random;

    @Inject
    public ConstantRateUncorrelatedUniformChurnProvider(final ProbabilityDistribution first_arrival_delay_distribution, final ProbabilityDistribution session_length_distribution, final ProbabilityDistribution downtime_distribution, final SeedProvider seed_provider) {

        this.first_arrival_delay_distribution = first_arrival_delay_distribution;
        this.session_length_distribution = session_length_distribution;
        this.downtime_distribution = downtime_distribution;
        this.seed_provider = seed_provider;
        uniform_random = new Random(seed_provider.get());
    }

    @Override
    public Churn get() {

        final Duration first_arrival_delay = generateFirstArrivalDelay();
        final Long seed = seed_provider.get();
        return new ConstantRateUncorrelatedChurn(first_arrival_delay, session_length_distribution, downtime_distribution, seed);
    }

    private Duration generateFirstArrivalDelay() {

        return RandomNumberGenerator.generateDurationInNanoseconds(first_arrival_delay_distribution, uniform_random);
    }
}
