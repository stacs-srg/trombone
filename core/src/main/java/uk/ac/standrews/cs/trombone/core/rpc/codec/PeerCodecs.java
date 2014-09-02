package uk.ac.standrews.cs.trombone.core.rpc.codec;

import org.mashti.jetson.lean.codec.Codecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerCodecs extends Codecs {

    private static final PeerCodecs INSTANCE = new PeerCodecs();

    public static PeerCodecs getInstance() {

        return INSTANCE;
    }

    private PeerCodecs() {

        register(0, new KeyCodec());
        register(1, new NextHopReferenceCodec());
        register(2, new PeerReferenceCodec());
        register(3, new PeerReferenceListCodec());
        register(4, new SelectorCodec());
    }
}
