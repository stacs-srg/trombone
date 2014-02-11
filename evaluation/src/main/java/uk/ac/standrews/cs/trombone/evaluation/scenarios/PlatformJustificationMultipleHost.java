package uk.ac.standrews.cs.trombone.evaluation.scenarios;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PlatformJustificationMultipleHost extends PlatformJustification {

    public PlatformJustificationMultipleHost(int host_count) {

        super(PlatformJustificationMultipleHost.class.getSimpleName() + host_count);

        for (int i = 0; i < host_count; i++) {
            addHost("compute-0-" + i + ".local", 1, Constants.PORT_NUMBER_PROVIDER.clone());
        }                                                          
    }
}
