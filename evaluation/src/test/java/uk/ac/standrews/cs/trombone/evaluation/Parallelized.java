package uk.ac.standrews.cs.trombone.evaluation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.runners.Parameterized;
import org.junit.runners.model.RunnerScheduler;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Parallelized extends Parameterized {

    /**
     * Only called reflectively. Do not use programmatically.
     *
     * @param klass
     */
    public Parallelized(final Class<?> klass) throws Throwable {

        super(klass);
        final ExecutorService executorService = Executors.newFixedThreadPool(100);
        setScheduler(new RunnerScheduler() {

            @Override
            public void schedule(final Runnable childStatement) {

                executorService.execute(childStatement);

            }

            @Override
            public void finished() {

                executorService.shutdown();
                try {
                    executorService.awaitTermination(800, TimeUnit.MINUTES);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
