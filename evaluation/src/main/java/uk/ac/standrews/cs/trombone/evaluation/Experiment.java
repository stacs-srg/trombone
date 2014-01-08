package uk.ac.standrews.cs.trombone.evaluation;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public abstract class Experiment {

    private final Scenario scenario;

    protected Experiment(Scenario scenario) {

        this.scenario = scenario;
    }

    public final void run() throws Exception {

        setup();
        try {
            
            
            
            execute();
        }
        finally {
            tearDown();
        }
    }

    protected abstract void execute() throws Exception;

    protected void setup() throws Exception {

    }

    protected void tearDown() throws Exception {

    }
}
