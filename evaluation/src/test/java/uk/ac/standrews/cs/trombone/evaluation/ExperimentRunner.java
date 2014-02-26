package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.host.LocalHost;
import uk.ac.standrews.cs.shabdiz.testing.junit.JUnitBootstrapCore;
import uk.ac.standrews.cs.shabdiz.testing.junit.ParameterizedRange;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.shabdiz.util.JarUtils;
import uk.ac.standrews.cs.shabdiz.util.ProcessUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ExperimentRunner extends Parameterized {

    static final String RESULT_PROPERTY_KEY = "RESULT";
    static final Duration TEST_OUTPUT_TIMEOUT = new Duration(1, TimeUnit.DAYS);
    static final int MAX_RETRY_COUNT = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentRunner.class);
    private final LocalHost local_host;
    private final String runner_jar_path;
    private int retry_count = 1;

    public ExperimentRunner(final Class<?> experiment_class) throws Throwable {

        super(experiment_class);
        local_host = new LocalHost();
        runner_jar_path = initRunnerJar().getAbsolutePath();
    }

    public static void main(String[] args) {

        final String args_as_string = Arrays.toString(args);
        Result result;
        try {
            final String test_class_name = args[0];
            final Class<?> test_class = Class.forName(test_class_name);
            final ParameterizedRange runner = new ParameterizedRange(test_class, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            final JUnitCore core = new JUnitCore();
            result = core.run(runner);
        }
        catch (Throwable e) {
            LOGGER.error("failed to run test with arguments {}" + args_as_string, e);
            result = new Result();
            final Description description = Description.createSuiteDescription(e.getMessage());
            final Failure failure = new Failure(description, e);
            result.getFailures().add(failure);
        }

        if (result.wasSuccessful()) {
            LOGGER.info("Experiment {} completed successfully", args_as_string);
        }
        else {
            LOGGER.error("Experiment {} had failures", args_as_string);
            for (Failure failure : result.getFailures()) {
                LOGGER.error(failure.getMessage(), failure.getException());
            }
        }

        int exit_code = 0;
        try {
            final String result_in_base64 = JUnitBootstrapCore.serializeAsBase64(result);
            ProcessUtil.printKeyValue(System.out, RESULT_PROPERTY_KEY, result_in_base64);
        }
        catch (IOException e) {
            LOGGER.error("failed to encode result to base64", e);
            exit_code = 1;
        }
        finally {
            System.exit(exit_code);
        }

    }

    private File initRunnerJar() throws IOException {

        final File runner_jar = File.createTempFile("experiment_runner", ".jar");

        JarUtils.currentClasspathToExecutableJar(runner_jar, ExperimentRunner.class);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {

                FileUtils.deleteQuietly(runner_jar);
            }
        }));
        runner_jar.deleteOnExit();
        return runner_jar;
    }

    @Override
    protected synchronized void runChild(final Runner runner, final RunNotifier notifier) {

        final int index = getRunnerIndex(runner);
        final Description description = runner.getDescription();
        final File working_directory = new File(".");

        boolean successful;

        try {
            final EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
            FileUtils.forceMkdir(working_directory);
            final String command = "java -jar " + runner_jar_path + " " + getTestClass().getName() + " " + index + " " + (index + 1);
            LOGGER.debug("running command {}", command);
            final Process test_process = local_host.execute(working_directory.getAbsolutePath(), command);
            final String result_in_base64;
            try {
                result_in_base64 = ProcessUtil.scanProcessOutput(test_process, RESULT_PROPERTY_KEY, TEST_OUTPUT_TIMEOUT);
            }
            finally {
                test_process.destroy();
            }

            eachNotifier.fireTestStarted();
            final List<Description> descriptions = getMethodDescriptions(runner);
            for (Description description1 : descriptions) {
                notifier.fireTestStarted(description1);
            }

            final Result result = JUnitBootstrapCore.deserializeAsBase64(result_in_base64);
            successful = result.wasSuccessful();
            if (successful) {
                eachNotifier.fireTestFinished();
            }

            final List<Throwable> failure_exceptions = new ArrayList<Throwable>();
            for (Failure failure : result.getFailures()) {
                failure_exceptions.add(failure.getException());
                notifier.fireTestFailure(failure);
                descriptions.remove(failure.getDescription());
            }

            if (!failure_exceptions.isEmpty()) {
                eachNotifier.addFailure(new MultipleFailureException(failure_exceptions));
            }
            for (Description description1 : descriptions) {
                notifier.fireTestFinished(description1);
            }
        }
        catch (Throwable e) {
            successful = false;
            notifier.fireTestFailure(new Failure(description, e));
        }

        if (!successful) {
            retry_count++;
            LOGGER.info("retrying {} with retry count {}", description, retry_count);

            if (retry_count <= MAX_RETRY_COUNT) {
                runChild(runner, notifier);
            }

        }
        retry_count = 1;
    }

    private List<Description> getMethodDescriptions(final Runner runner) {

        final String displayName = runner.getDescription().getDisplayName();
        final List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Test.class);
        List<Description> descriptions = new ArrayList<Description>();
        for (FrameworkMethod method : methods) {
            final Description description = Description.createTestDescription(getTestClass().getJavaClass(), method.getName() + displayName, method.getAnnotations());
            descriptions.add(description);
        }

        return descriptions;
    }

    private int getRunnerIndex(Runner runner) {

        int i = 0;
        for (Runner child : getChildren()) {
            if (runner.equals(child)) { return i; }
            i++;
        }
        throw new NoSuchElementException("No matching runner " + runner);
    }
}
