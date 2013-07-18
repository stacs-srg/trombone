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
package uk.ac.standrews.cs.trombone.trombone.workload;

import com.google.inject.Provider;
import javax.inject.Inject;
import javax.inject.Singleton;
import uk.ac.standrews.cs.trombone.trombone.key.RandomIntegerKeyProvider;
import uk.ac.standrews.cs.trombone.trombone.math.ProbabilityDistribution;
import uk.ac.standrews.cs.trombone.trombone.util.SeedProvider;

@Singleton
public class ConstantRateWorkloadProvider implements Provider<Workload> {

    private final ProbabilityDistribution intervals_dsitribution;
    private final int retry_threshold;
    private final SeedProvider seed_provider;

    @Inject
    public ConstantRateWorkloadProvider(final ProbabilityDistribution intervals_dsitribution, final int retry_threshold, final SeedProvider seed_provider) {

        this.intervals_dsitribution = intervals_dsitribution;
        this.retry_threshold = retry_threshold;
        this.seed_provider = seed_provider;
    }

    @Override
    public Workload get() {

        final Long seed = seed_provider.get();
        return new ConstantRateWorkload(intervals_dsitribution, RandomIntegerKeyProvider.getDefault(), retry_threshold, seed);
    }
}
