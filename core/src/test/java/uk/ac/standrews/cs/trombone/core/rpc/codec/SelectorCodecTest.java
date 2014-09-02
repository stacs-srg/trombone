package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.selector.ChordPredecessorSelector;
import uk.ac.standrews.cs.trombone.core.selector.ChordSuccessorListSelector;
import uk.ac.standrews.cs.trombone.core.selector.ChordSuccessorSelector;
import uk.ac.standrews.cs.trombone.core.selector.EmptySelector;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.Last;
import uk.ac.standrews.cs.trombone.core.selector.RandomSelector;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.selector.Self;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SelectorCodecTest {

    private SelectorCodec codec;
    private ByteBuf buffer;

    @Before
    public void setUp() throws Exception {

        codec = new SelectorCodec();
        buffer = Unpooled.buffer();
    }

    @Test
    public void testIsSupported() throws Exception {

        assertTrue(codec.isSupported(Selector.class));
        assertFalse(codec.isSupported(String.class));
        assertFalse(codec.isSupported(null));
    }

    @Test
    public void testCodec() throws Exception {

        final Selector[] keys = {
                null, ChordPredecessorSelector.getInstance(), new ChordSuccessorListSelector(10), ChordSuccessorSelector.getInstance(), EmptySelector.getInstance(),
                new First(10, Selector.ReachabilityCriteria.ANY), new First(1, Selector.ReachabilityCriteria.UNREACHABLE), new Last(10, Selector.ReachabilityCriteria.ANY),
                new Last(1, Selector.ReachabilityCriteria.UNREACHABLE), new RandomSelector(10, Selector.ReachabilityCriteria.ANY), new RandomSelector(1, Selector.ReachabilityCriteria.UNREACHABLE),
                Self.getInstance()
        };

        for (Selector key : keys) {
            codec.encode(key, buffer, PeerCodecs.getInstance(), Selector.class);
            assertEquals(key, codec.decode(buffer, PeerCodecs.getInstance(), Selector.class));
        }
    }

}
