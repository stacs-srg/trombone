package uk.ac.standrews.cs.trombone.core.key;

import javax.inject.Provider;
import uk.ac.standrews.cs.trombone.core.util.Repeatable;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface KeyProvider extends Provider<Key>, Repeatable, Cloneable {

    KeyProvider clone();
}
