package uk.ac.standrews.cs.trombone.evaluation.scenarios;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PlatformJustificationSingleHost extends PlatformJustification {

    public PlatformJustificationSingleHost(int host_count) {

        super(PlatformJustificationSingleHost.class.getSimpleName() + host_count);
        addHost("compute-0-0.local", host_count, Constants.PORT_NUMBER_PROVIDER.clone());
    }
}
