package uk.ac.standrews.cs.trombone.event.provider;

import javax.inject.Provider;
import uk.ac.standrews.cs.trombone.event.churn.Churn;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class NoChurnProvider implements Provider<Churn> {

    private static final NoChurnProvider SINGLETON_NO_CHURN_PROVIDER = new NoChurnProvider();

    private NoChurnProvider() {

    }

    public static NoChurnProvider getInstance() {

        return SINGLETON_NO_CHURN_PROVIDER;
    }

    @Override
    public Churn get() {

        return Churn.NONE;
    }

    @Override
    public String toString() {

        return "none";
    }
}
