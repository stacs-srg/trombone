package uk.ac.standrews.cs.trombone.evaluation.provider;

import javax.inject.Provider;
import uk.ac.standrews.cs.trombone.evaluation.churn.Churn;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class NoChurnProvider implements Provider<Churn> {

    @Override
    public Churn get() {

        return Churn.NONE;
    }
}
