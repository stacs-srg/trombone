package uk.ac.standrews.cs.trombone.evaluation.job;

import java.io.Serializable;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.job.util.Attributes;
import uk.ac.standrews.cs.shabdiz.job.util.SerializableVoid;
import uk.ac.standrews.cs.shabdiz.util.AttributeKey;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SetAttributeJob<Value extends Serializable> implements Job<SerializableVoid> {

    private final AttributeKey<Value> key;
    private final Value value;

    public SetAttributeJob(AttributeKey<Value> key, Value value) {

        this.key = key;
        this.value = value;
    }

    @Override
    public SerializableVoid call() throws Exception {

        Attributes.put(key, value);
        return null; // Void job
    }
}
