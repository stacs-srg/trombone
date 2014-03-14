package uk.ac.standrews.cs.trombone.core.rpc.codec;

import java.lang.reflect.Type;
import java.util.ArrayList;
import org.mashti.jetson.lean.codec.CollectionCodec;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerReferenceListCodec extends CollectionCodec {

    @Override
    public boolean isSupported(final Type type) {

        return super.isSupported(type) && PeerReference.class.isAssignableFrom((Class<?>) getComponentType(type));
    }

    @Override
    protected ArrayList<PeerReference> constructCollectionOfType(final Type type) {

        return new ArrayList<PeerReference>();
    }
}
