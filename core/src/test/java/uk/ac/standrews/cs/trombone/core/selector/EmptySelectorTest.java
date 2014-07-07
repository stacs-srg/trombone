package uk.ac.standrews.cs.trombone.core.selector;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.PeerReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class EmptySelectorTest {

    @Test
    public void testSelect() throws Exception {

        final List<PeerReference> empty = Collections.emptyList();
        assertEquals(empty, EmptySelector.INSTANCE.select(null));

    }

    @Test
    public void testCopy() throws Exception {

        assertSame(EmptySelector.INSTANCE, EmptySelector.INSTANCE.copy());
    }

    @Test
    public void testIsSingleton() throws Exception {

        assertTrue(EmptySelector.INSTANCE.isSingleton());
    }
}