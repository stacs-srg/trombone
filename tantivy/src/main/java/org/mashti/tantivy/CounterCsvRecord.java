package org.mashti.tantivy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class CounterCsvRecord {

    private long timestamp;
    private long count;

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(final long timestamp) {

        this.timestamp = timestamp;
    }

    public long getCount() {

        return count;
    }

    public void setCount(final long count) {

        this.count = count;
    }
}
