package uk.ac.standrews.cs.trombone.evaluation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.runners.Parameterized;
import org.junit.runners.model.RunnerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Parallelized extends Parameterized {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parallelized.class);

    public Parallelized(final Class<?> test_class) throws Throwable {

        super(test_class);
        final ExecutorService executor = Executors.newFixedThreadPool(200);
        setScheduler(new RunnerScheduler() {

            @Override
            public void schedule(final Runnable childStatement) {

                executor.execute(childStatement);
            }

            @Override
            public void finished() {

                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                }
                catch (InterruptedException e) {
                    LOGGER.error("interrupted wile waiting for executor to terminate", e);
                }
            }
        });
    }
}
