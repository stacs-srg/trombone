package uk.ac.standrews.cs.trombone.event;

import java.io.Closeable;
import java.util.Iterator;
import org.json.JSONObject;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface EventReader extends Closeable, Iterator<Event> {

    PeerConfiguration getConfiguration(PeerReference event_source);

    JSONObject getScenario();
}
