package uk.ac.standrews.cs.trombone.core;

import java.io.Serializable;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface PeerConfiguration extends Serializable {

    Maintenance getMaintenance(Peer peer);

    SyntheticDelay getSyntheticDelay();
}
