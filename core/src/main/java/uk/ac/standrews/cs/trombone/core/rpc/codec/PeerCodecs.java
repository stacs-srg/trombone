package uk.ac.standrews.cs.trombone.core.rpc.codec;

import org.mashti.jetson.lean.codec.Codecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerCodecs extends Codecs {

    public static final PeerCodecs INSTANCE = new PeerCodecs();

    private PeerCodecs() {

        register(0, new KeyCodec());
        register(1, new PeerReferenceCodec());
    }

}
