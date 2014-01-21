package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javax.inject.Provider;
import org.mashti.sina.distribution.ZipfDistribution;
import org.uncommons.maths.random.MersenneTwisterRNG;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ZipfKeyProvider implements Provider<Key> {

    private final ZipfDistribution distribution;
    private final Key[] keys;
    private final Random random;
    private final int elements_count;
    private final LinkedBlockingQueue<Key> keys_queue = new LinkedBlockingQueue<>(10000);

    public ZipfKeyProvider(final int elements_count, double exponent, long seed) {

        this.elements_count = elements_count;
        distribution = new ZipfDistribution(elements_count, exponent);
        keys = new RandomKeyProvider(seed).generate(elements_count);
        random = new MersenneTwisterRNG(); //TODO use Well19937c
        final ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            executorService.submit(new Callable<Object>() {

                @Override
                public Object call() throws Exception {

                    final MersenneTwisterRNG rng = new MersenneTwisterRNG();
                    while (!Thread.currentThread().isInterrupted()) {
                        final Key key = keys[nextIndex(rng)];
                        keys_queue.put(key);
                    }

                    return null;
                }
            });
        }
    }

    @Override
    public Key get() {

        try {
            return keys_queue.take();
        }
        catch (Throwable e) {
            return keys[nextIndex()];
        }
    }

    private int nextIndex() {

        return nextIndex(random);
    }

    private int nextIndex(Random random) {

        int rank;
        double frequency;
        double dice;
        do {
            rank = random.nextInt(elements_count) + 1;
            frequency = distribution.density(rank).doubleValue();
            dice = random.nextDouble();
        } while (!(dice < frequency));

        return rank - 1;
    }
}
