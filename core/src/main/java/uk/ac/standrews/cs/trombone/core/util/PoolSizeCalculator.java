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
package uk.ac.standrews.cs.trombone.core.util;

/**
 * The Class PoolSizeCalculator.
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class PoolSizeCalculator {

    private static final int N_CPUS = Runtime.getRuntime().availableProcessors();
    private static final float DEFAULT_CPU_UTILIZATION_RATIO = 1.0f;

    private PoolSizeCalculator() {

    }

    /**
     * Calculates thread pool size based on Brian Goetz' optimal thread count formula, see 'Java Concurrency in Practice' (chapter 8.2), with CPU utilisation ratio of <i>{@code 1.0}</i>.
     * @param wait_to_compute_time_ratio the wait time of a task divided by the CPU time consumed by that task
     * @return the thread pool size based on Brian Goetz' optimal thread count formula
     */
    public static int calculatePoolSize(final float wait_to_compute_time_ratio) {

        return calculatePoolSize(DEFAULT_CPU_UTILIZATION_RATIO, wait_to_compute_time_ratio);
    }

    /**
     * Calculates thread pool size based on Brian Goetz' optimal thread count formula, see 'Java Concurrency in Practice' (chapter 8.2).
     * @param cpu_utilization_ratio CUP utilisation of the system
     * @param wait_to_compute_time_ratio the wait time of a task divided by the CPU time consumed by that task
     * @return the thread pool size based on Brian Goetz' optimal thread count formula
     */
    public static int calculatePoolSize(final float cpu_utilization_ratio, final float wait_to_compute_time_ratio) {

        if (cpu_utilization_ratio < 0 || cpu_utilization_ratio > 1) { throw new IllegalArgumentException("CPU utilisation ratio must be 0<= cpu_utilization_ratio <= 1"); }

        return N_CPUS * (int) (cpu_utilization_ratio * (1 + wait_to_compute_time_ratio));
    }
}
