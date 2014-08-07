package uk.ac.standrews.cs.trombone.core.maintenance;

import java.util.function.Function;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface MaintenanceFactory extends Function<Peer, Maintenance> {

    @Override
    Maintenance apply(Peer peer);
}
