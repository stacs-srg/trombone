package uk.ac.standrews.cs.trombone.core.state;

import java.math.BigInteger;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordPeerStateFactory implements PeerStateFactory {

    private final int finger_table_size;
    private final BigInteger finger_base;
    private final int successor_list_size;

    public ChordPeerStateFactory(final int finger_table_size, final BigInteger finger_base, final int successor_list_size) {

        this.finger_table_size = finger_table_size;
        this.finger_base = finger_base;
        this.successor_list_size = successor_list_size;
    }

    @Override
    public PeerState apply(final Peer peer) {

        return new ChordPeerState(peer, finger_table_size, finger_base, successor_list_size);
    }

    public int getFingerTableSize() {

        return finger_table_size;
    }

    public BigInteger getFingerBase() {

        return finger_base;
    }

    public int getSuccessorListSize() {

        return successor_list_size;
    }
}
