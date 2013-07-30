package uk.ac.standrews.cs.trombone.evaluation.provider;

import java.net.UnknownHostException;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerProvider implements SerializableProvider<Peer> {

    private static final long serialVersionUID = 3337629503583293865L;
    private final SerializableProvider<Key> key_provider;

    public PeerProvider(SerializableProvider<Key> key_provider) {

        this.key_provider = key_provider;
    }

    @Override
    public Peer get() {

        final Key key = key_provider.get();
        try {
            return new Peer(key);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException("failed to provide peer", e);
        }
    }
}
