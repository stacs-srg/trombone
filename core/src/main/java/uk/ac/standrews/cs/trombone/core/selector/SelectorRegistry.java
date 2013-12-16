package uk.ac.standrews.cs.trombone.core.selector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class SelectorRegistry {

    private static final Set<Selector> SELECTORS = new HashSet<>();

    private SelectorRegistry() {

    }

    public static boolean register(Selector selector) {

        synchronized (SELECTORS) {
            return SELECTORS.add(selector);
        }
    }

    public static Selector pickRandomly(Random random) {

        synchronized (SELECTORS) {
            final int candidate_index = random.nextInt(SELECTORS.size());
            int index = 0;
            for (Selector selector : SELECTORS) {

                if (index == candidate_index) {
                    return selector;
                }
                index++;
            }
            throw new IllegalStateException("possible concurrency bug");
        }
    }
}
