package uk.ac.standrews.cs.trombone.key;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import java.util.Random;
import javax.inject.Provider;
import uk.ac.standrews.cs.trombone.math.ZipfDistribution;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ZipfIntegerKeyProvider implements Provider<Key> {

    private final ZipfDistribution distribution;
    private final IntegerKey[] keys;
    private final Random random;
    private final int elements_count;

    public ZipfIntegerKeyProvider(int elements_count, double exponent, long seed) {
        this.elements_count = elements_count;

        distribution = new ZipfDistribution(elements_count, exponent);
        keys = new RandomIntegerKeyProvider(seed).generate(elements_count);
        random = new Random(seed); //TODO use Well19937c

    }

    public static void main(String[] args) {
        ZipfIntegerKeyProvider provider = new ZipfIntegerKeyProvider(1000, 1, 88);
        long time = System.nanoTime();
        TreeMultiset<Integer> m = TreeMultiset.create();
        for (int i = 0; i < 1000; i++) {
            m.add(provider.nextIndex());
            System.out.println();
            //            System.out.println(provider.distribution.sample());
        }

        System.out.println("time: " + (System.nanoTime() - time));

        for (Multiset.Entry<Integer> ss : m.entrySet()) {
            System.out.println(ss);
        }

    }

    @Override
    public synchronized IntegerKey get() {

        return keys[nextIndex()];
    }

    private int nextIndex() {
        int rank;
        double friquency;
        double dice;
        do {
            rank = random.nextInt(elements_count) + 1;
            friquency = distribution.probability(rank).doubleValue();
            dice = random.nextDouble();
        } while (!(dice < friquency));

        return rank - 1;
    }
}
