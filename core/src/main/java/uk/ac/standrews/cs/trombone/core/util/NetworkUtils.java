package uk.ac.standrews.cs.trombone.core.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class NetworkUtils {

    private static final String LOCAL_HOST_NAME;

    static {
        try {
            LOCAL_HOST_NAME = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            throw new RuntimeException("unable to determine the name of local host", e);
        }
    }

    private NetworkUtils() {

    }

    public static boolean isLocalAddress(InetAddress address) {

        boolean local = address.isAnyLocalAddress() || address.isLoopbackAddress() || LOCAL_HOST_NAME.equals(address.getHostName());
        if (!local) {
            try {
                local = NetworkInterface.getByInetAddress(address) != null;
            }
            catch (final SocketException e) {
                local = false;
            }
        }
        return local;
    }

}
