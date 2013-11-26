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

import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.trombone.core.key.RandomIntegerKeyProvider;
import uk.ac.standrews.cs.trombone.workload.ConstantRateWorkload;
import uk.ac.standrews.cs.trombone.workload.Workload;

public class ConstantRateWorkloadProvider implements SerializableProvider<Workload> {

    private final ProbabilityDistribution intervals_dsitribution;
    private final int retry_threshold;
    private final SerializableProvider<Long> seed_provider;
    private final RandomIntegerKeyProvider key_provider;

    public ConstantRateWorkloadProvider(final ProbabilityDistribution intervals_dsitribution, final int retry_threshold, final SerializableProvider<Long> seed_provider) {

        this.intervals_dsitribution = intervals_dsitribution;
        this.retry_threshold = retry_threshold;
        this.seed_provider = seed_provider;
        key_provider = new RandomIntegerKeyProvider(seed_provider.get());
    }

    @Override
    public Workload get() {

        final Long seed = seed_provider.get();
        return new ConstantRateWorkload(intervals_dsitribution, key_provider, retry_threshold, seed);
    }
}
