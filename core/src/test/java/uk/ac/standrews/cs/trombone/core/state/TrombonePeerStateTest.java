package uk.ac.standrews.cs.trombone.core.state;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;

public class TrombonePeerStateTest {

    private static final Random RANDOM = new Random(87542);
    private static final Supplier<Key> KEY_PROVIDER = () -> Key.valueOf(RANDOM.nextLong());
    public static final int STATE_SIZE = 100;
    private TrombonePeerState state;
    private Key local_key;

    private static final Logger LOGGER = LoggerFactory.getLogger(TrombonePeerStateTest.class);

    @Before
    public void setUp() throws Exception {

        local_key = KEY_PROVIDER.get();
        state = new TrombonePeerState(local_key);

        for (int i = 0; i < STATE_SIZE; i++) {
            state.add(new PeerReference(KEY_PROVIDER.get(), new InetSocketAddress(0)));
        }
    }

    @Test
    public void testGetInternalReference() throws Exception {

    }

    @Test
    public void testInLocalKeyRange() throws Exception {

        Assert.assertTrue(state.inLocalKeyRange(local_key));
        Assert.assertFalse(state.inLocalKeyRange(local_key.next()));

        final Key first_key = state.first()
                .getKey();
        final Key last_key = state.last()
                .getKey();

        LOGGER.info("first key {}", first_key);
        LOGGER.info("last key {}", last_key);

        Assert.assertFalse(state.inLocalKeyRange(first_key));
        Assert.assertFalse(state.inLocalKeyRange(last_key));
        Assert.assertTrue(state.inLocalKeyRange(last_key.next()));
    }

    @Test
    public void testAdd() throws Exception {

    }

    @Test
    public void testRemove() throws Exception {

    }

    @Test
    public void testLower() throws Exception {

        final Key[] next = {
                state.last()
                        .getKey()
                        .next()
        };

        final List<PeerReference> references = state.getInternalReferences();
        Collections.reverse(references);

        references.forEach(reference -> {
            final PeerReference lower = state.lower(next[0]);
            if (next[0].equals(state.first()
                    .getKey())) {
                Assert.assertNull(lower);
            }
            else {
                Assert.assertEquals(reference, lower);
                next[0] = lower.getKey();
            }
        });

    }

    @Test
    public void testHigher() throws Exception {

        final Key[] next = {local_key};

        state.stream()
                .forEach(reference -> {
                    final PeerReference higher = state.higher(next[0]);
                    if (next[0].equals(state.last()
                            .getKey())) {
                        Assert.assertNull(higher);
                    }
                    else {
                        Assert.assertEquals(reference, higher);
                        next[0] = higher.getKey();
                    }
                });

    }

    @Test
    public void testCeilingReachable() throws Exception {

    }

    @Test
    public void testCeiling() throws Exception {

    }

    @Test
    public void testFirst() throws Exception {

    }

    @Test
    public void testFirstReachable() throws Exception {

    }

    @Test
    public void testLast() throws Exception {

    }

    @Test
    public void testLastReachable() throws Exception {

    }

    @Test
    public void testGetReferences() throws Exception {

    }

    @Test
    public void testSize() throws Exception {

        Assert.assertEquals(STATE_SIZE, state.size());
    }

    @Test
    public void testGetValues() throws Exception {

    }

    @Test
    public void testGetInternalReferences() throws Exception {

    }
}