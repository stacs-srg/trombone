package uk.ac.standrews.cs.trombone.evaluation.provider;

import uk.ac.standrews.cs.trombone.churn.Churn;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class NoChurnProvider implements SerializableProvider<Churn> {

    private static final NoChurnProvider INSTANCE = new NoChurnProvider();
    private static final long serialVersionUID = 5828215120177976246L;

    private NoChurnProvider() {

    }

    public static NoChurnProvider getInstance() {

        return INSTANCE;
    }

    @Override
    public Churn get() {

        return Churn.NONE;
    }
}
