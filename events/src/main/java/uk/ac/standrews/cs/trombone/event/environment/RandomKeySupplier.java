package uk.ac.standrews.cs.trombone.event.environment;

import java.util.Random;
import java.util.function.Supplier;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import uk.ac.standrews.cs.trombone.core.Key;

/**
 * Supplies random deterministic {@link Key keys}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomKeySupplier implements Supplier<Key> {

    protected final long seed;
    protected final Random random;

    /**
     * Constructs a copy of the given {@code key_factory}.
     *
     * @param key_factory the factory of which to construct a copy
     */
    public RandomKeySupplier(RandomKeySupplier key_factory) {

        this(key_factory.seed);
    }

    public RandomKeySupplier(long seed) {

        this.seed = seed;

        random = new RandomAdaptor(new MersenneTwister(seed));
    }

    static Key generate(Random random) {

        final byte[] key_value = new byte[Key.KEY_LENGTH];
        random.nextBytes(key_value);
        return Key.valueOf(key_value);
    }

    static Key[] generate(final int count, final Random random) {

        final Key[] keys = new Key[count];
        for (int i = 0; i < count; i++) {
            keys[i] = generate(random);
        }
        return keys;
    }

    @Override
    public Key get() {

        return generate(random);
    }

    public long getSeed() {

        return seed;
    }
}
