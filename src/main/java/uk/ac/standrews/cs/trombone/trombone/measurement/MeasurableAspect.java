package uk.ac.standrews.cs.trombone.trombone.measurement;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
enum MeasurableAspect {

    /** The peer state size. */
    PEER_STATE_SIZE,

    /** The join count. */
    PEER_ARRIVAL_COUNT,

    /** The leave count. */
    PEER_DEPARTURE_COUNT,

    /** The bytes sent per node. */
    SENT_BITS_PER_PEER,

    /** The lookup correct delay. */
    LOOKUP_CORRECT_DELAY,

    /** The lookup correct hop count. */
    LOOKUP_CORRECT_HOP_COUNT,

    /** The lookup correct retry count. */
    LOOKUP_CORRECT_RETRY_COUNT,

    /** The lookup correct count. */
    LOOKUP_CORRECT_COUNT,

    /** The lookup error count. */
    LOOKUP_ERROR_COUNT,

    /** The lookup incorrect count. */
    LOOKUP_INCORRECT_COUNT,

    /** The metadata about experiment. */
    METADATA;
}
