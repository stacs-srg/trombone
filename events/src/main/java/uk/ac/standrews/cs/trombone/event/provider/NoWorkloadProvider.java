package uk.ac.standrews.cs.trombone.event.provider;

import javax.inject.Provider;
import uk.ac.standrews.cs.trombone.event.workload.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class NoWorkloadProvider implements Provider<Workload> {

    private static final NoWorkloadProvider NO_WORKLOAD_PROVIDER_INSTANCE = new NoWorkloadProvider();

    private NoWorkloadProvider() {

    }

    public static NoWorkloadProvider getInstance() {

        return NO_WORKLOAD_PROVIDER_INSTANCE;
    }

    @Override
    public Workload get() {

        return Workload.NONE;
    }

    @Override
    public String toString() {

        return "none";
    }
}
