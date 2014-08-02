package uk.ac.standrews.cs.trombone.core;

import java.net.InetAddress;
import org.junit.Assert;
import org.junit.Test;

public class ZeroSyntheticDelayTest {

    @Test
    public void testGet() throws Exception {

        Assert.assertEquals(0, SyntheticDelay.ZERO.get(null, null, null));
        Assert.assertEquals(0, SyntheticDelay.ZERO.get(InetAddress.getLocalHost(), InetAddress.getLocalHost(), null));
        Assert.assertEquals(0, SyntheticDelay.ZERO.get(InetAddress.getLocalHost(), null, null));
    }
}