package uk.ac.standrews.cs.trombone.math;

import com.google.common.util.concurrent.AtomicDoubleArray;
import io.netty.util.internal.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A random sampling reservoir of a stream of {@code long}s. Uses Vitter's Algorithm R to produce a
 * statistically representative sample.
 *
 * @see <a href="http://www.cs.umd.edu/~samir/498/vitter.pdf">Random Sampling with a Reservoir</a>
 */
public class UniformReservoir {

    private static final int DEFAULT_SIZE = 1028;
    private static final int BITS_PER_LONG = 63;
    private final AtomicLong count = new AtomicLong();
    private final AtomicDoubleArray values;

    /**
     * Creates a new {@link UniformReservoir} of 1028 elements, which offers a 99.9% confidence level
     * with a 5% margin of error assuming a normal distribution.
     */
    public UniformReservoir() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a new {@link UniformReservoir}.
     *
     * @param size the number of samples to keep in the sampling reservoir
     */
    public UniformReservoir(int size) {
        values = new AtomicDoubleArray(size);
        reset();
    }

    public int size() {
        final long c = count.get();
        final int values_length = values.length();
        if (c > values_length) {
            return values_length;
        }
        return (int) c;
    }

    public void update(Number value) {
        update(value.doubleValue());
    }

    public void update(double value) {
        final long c = count.incrementAndGet();
        if (c <= values.length()) {
            values.set((int) c - 1, value);
        }
        else {
            final long r = nextLong(c);
            if (r < values.length()) {
                values.set((int) r, value);
            }
        }
    }

    public List<Double> getSnapshot() {
        final int s = size();
        final List<Double> copy = new ArrayList<Double>(s);
        for (int i = 0; i < s; i++) {
            copy.add(values.get(i));
        }
        return copy;
    }

    public void reset() {
        for (int i = 0; i < values.length(); i++) {
            values.set(i, 0);
        }
        count.set(0);
    }

    /**
     * Get a pseudo-random long uniformly between 0 and n-1. Stolen from
     * {@link java.util.Random#nextInt()}.
     *
     * @param n the bound
     * @return a value select randomly from the range {@code [0..n)}.
     */
    private static long nextLong(long n) {
        long bits, val;
        do {
            bits = ThreadLocalRandom.current().nextLong() & (~(1L << BITS_PER_LONG));
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }
}
