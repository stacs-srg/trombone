package uk.ac.standrews.cs.trombone.core;

import java.io.Serializable;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerConfiguration implements Serializable {

    private final Maintenance maintenance;
    private final SyntheticDelay synthetic_delay;

    public PeerConfiguration(Maintenance maintenance, SyntheticDelay synthetic_delay) {

        this.maintenance = maintenance;
        this.synthetic_delay = synthetic_delay;
    }

    public Maintenance getMaintenance() {

        return maintenance;
    }

    public SyntheticDelay getSyntheticDelay() {

        return synthetic_delay;
    }
}
