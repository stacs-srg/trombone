package uk.ac.standrews.cs.trombone.event;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface EventWriter extends Closeable {

    void write(Event event) throws IOException;
    void write(Scenario scenario) throws IOException;
}
