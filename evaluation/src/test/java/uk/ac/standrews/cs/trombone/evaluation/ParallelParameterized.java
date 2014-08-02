package uk.ac.standrews.cs.trombone.evaluation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.runners.Parameterized;
import org.junit.runners.model.RunnerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs {@link Parameterized parameterized} JUnit tests in parallel in a {@link ThreadPoolExecutor thread pool}.
 * The thread pool size is configured using the {@value #THREAD_POOL_SIZE_PROPERTY_KEY} System property.
 * If this property is not present or has an invalid value, a {@link Executors#newCachedThreadPool() cached thread pool} is used instead.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ParallelParameterized extends Parameterized {

    /**
     * The system property key that specifies the size of the thread pool of this runner.
     */
    public static final String THREAD_POOL_SIZE_PROPERTY_KEY = "experiment.runner.parallel_parameterized.pool_size";

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelParameterized.class);

    public ParallelParameterized(final Class<?> test_class) throws Throwable {

        super(test_class);
        setScheduler(new ParallelRunnerScheduler(constructExecutor()));
    }

    /**
     * Constructs the {@link ExecutorService executor} that is used for parallel test execution.
     * The thread pool size is configured using the {@value #THREAD_POOL_SIZE_PROPERTY_KEY} System property.
     * If this property is not present or has an invalid value, a {@link Executors#newCachedThreadPool() cached thread pool} is used.
     *
     * @return the executor of this runner
     */
    protected ExecutorService constructExecutor() {

        final int thread_pool_size = getThreadPoolSize();
        final AtomicLong thread_count = new AtomicLong();
        final String thread_name_prefix = getTestClass().getName();
        final ThreadFactory thread_factory = runnable -> {

            final String name = String.format("parallel_%s_%d", thread_name_prefix, thread_count.incrementAndGet());
            return new Thread(runnable, name);
        };

        return thread_pool_size == 0 ? Executors.newCachedThreadPool(thread_factory) : Executors.newFixedThreadPool(thread_pool_size, thread_factory);
    }

    private static int getThreadPoolSize() {

        try {
            return Integer.parseInt(System.getProperty(THREAD_POOL_SIZE_PROPERTY_KEY));
        }
        catch (final NumberFormatException e) {
            return 0;
        }
    }

    private static class ParallelRunnerScheduler implements RunnerScheduler {

        private final ExecutorService executor;

        public ParallelRunnerScheduler(final ExecutorService executor) {

            this.executor = executor;
        }

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
            catch (final InterruptedException e) {
                LOGGER.error("interrupted wile waiting for parallel test executions to finish", e);
            }
        }
    }
}
