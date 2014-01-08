package uk.ac.standrews.cs.trombone.evaluation.util;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ExperimentWatcher extends TestWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentWatcher.class);

    @Override
    protected void starting(final Description description) {

        super.starting(description);
        LOGGER.info("starting experiment: {}", description);
    }

    @Override
    protected void succeeded(final Description description) {

        super.succeeded(description);
        LOGGER.info("succeeded test {}", description);
    }

    @Override
    protected void failed(final Throwable e, final Description description) {

        super.failed(e, description);
        LOGGER.error("failed test {} due to error", description);
        LOGGER.error("failed test error", e);
    }

    @Override
    protected void skipped(final AssumptionViolatedException e, final Description description) {

        super.skipped(e, description);
        LOGGER.warn("skipped test {} due to assumption violation", description);
        LOGGER.warn("assumption violation", e);
    }

    @Override
    protected void finished(final Description description) {

        super.finished(description);

        LOGGER.info("finished experiment {}", description);
    }

}
