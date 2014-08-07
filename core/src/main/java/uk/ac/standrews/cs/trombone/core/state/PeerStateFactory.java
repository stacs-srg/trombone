package uk.ac.standrews.cs.trombone.core.state;

import java.util.function.Function;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface PeerStateFactory extends Function<Peer, PeerState> {

}
