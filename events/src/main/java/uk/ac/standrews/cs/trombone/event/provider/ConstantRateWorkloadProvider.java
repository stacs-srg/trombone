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
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.event.workload.ConstantRateWorkload;
import uk.ac.standrews.cs.trombone.event.workload.Workload;

public class ConstantRateWorkloadProvider implements Provider<Workload> {

    private final ProbabilityDistribution intervals_distribution;
    private final Provider<Long> seed_provider;
    private final Provider<Key> target_key_provider;

    public ConstantRateWorkloadProvider(final ProbabilityDistribution intervals_distribution, final Provider<Key> target_key_provider, final Provider<Long> seed_provider) {

        this.intervals_distribution = intervals_distribution;
        this.target_key_provider = target_key_provider;
        this.seed_provider = seed_provider;
    }

    @Override
    public Workload get() {

        final Long seed = seed_provider.get();
        return new ConstantRateWorkload(intervals_distribution, target_key_provider, seed);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("ConstantRateWorkloadProvider{");
        sb.append("intervals_distribution=").append(intervals_distribution);
        sb.append(", target_key_provider=").append(target_key_provider);
        sb.append('}');
        return sb.toString();
    }
}
