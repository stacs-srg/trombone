package uk.ac.standrews.cs.trombone.core.util;

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

        if (elements.isEmpty()) {
            LOGGER.warn("no Elements to choose from");
            return null;
        }

        final int selection_index = random.nextInt(elements.size());
        int index = 0;
        for (Element next : elements) {

            if (index == selection_index) {
                return next;
            }
            index++;
        }

        LOGGER.warn("set of elements was modified while picking at random");
        return null;
    }
}
