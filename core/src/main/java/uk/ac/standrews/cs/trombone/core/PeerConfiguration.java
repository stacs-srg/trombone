package uk.ac.standrews.cs.trombone.core;

import java.io.Serializable;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerConfiguration implements Serializable {

    private static final long serialVersionUID = -6034624232106255574L;
    private final MaintenanceFactory maintenance;
    private final SyntheticDelay synthetic_delay;
    private boolean application_feedback_enabled;

    public PeerConfiguration(MaintenanceFactory maintenance, SyntheticDelay synthetic_delay) {

        this.maintenance = maintenance;
        this.synthetic_delay = synthetic_delay;
    }

    public MaintenanceFactory getMaintenanceFactory() {

        return maintenance;
    }

    public SyntheticDelay getSyntheticDelay() {

        return synthetic_delay;
    }

    public boolean isApplicationFeedbackEnabled() {

        return application_feedback_enabled;
    }

    public void setApplicationFeedbackEnabled(final boolean application_feedback_enabled) {

        this.application_feedback_enabled = application_feedback_enabled;
    }
}
