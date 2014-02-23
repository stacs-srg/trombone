package uk.ac.standrews.cs.trombone.core.util;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class NamingUtils {

    public static String name(Object object) {

        return object == null ? "NULL" : object.getClass().getSimpleName();
        
    }
}
