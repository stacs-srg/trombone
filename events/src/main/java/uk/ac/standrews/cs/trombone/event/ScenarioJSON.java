package uk.ac.standrews.cs.trombone.event;

import org.json.JSONObject;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ScenarioJSON {

    public static JSONObject toJSON(Scenario scenario) {

        return new JSONObject(scenario);
    }

    
}
