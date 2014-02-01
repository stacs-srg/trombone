package uk.ac.standrews.cs.trombone.core.util;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class SelectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionUtil.class);

    private SelectionUtil() {

    }

    public static <Element> Element selectRandomly(Set<Element> elements, Random random) {

        while (!Thread.currentThread().isInterrupted()) {

            if (elements.isEmpty()) { return null;}
            final Iterator<Element> element_iterator = elements.iterator();
            final int selection_index = random.nextInt(elements.size());

            int index = 0;
            while (element_iterator.hasNext()) {
                Element next = element_iterator.next();
                if (index == selection_index) {
                    return next;
                }
                index++;
            }
            LOGGER.trace("set of elements was modified while picking at random. retrying random selection");
        }

        LOGGER.trace("random selection was interrupted. returning null");
        return null;
    }
}
