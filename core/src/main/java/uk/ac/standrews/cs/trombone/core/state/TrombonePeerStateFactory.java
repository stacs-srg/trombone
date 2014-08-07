package uk.ac.standrews.cs.trombone.core.state;

import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class TrombonePeerStateFactory implements PeerStateFactory {

    @Override
    public PeerState apply(final Peer peer) {

        return new TrombonePeerState(peer);
    }
}
