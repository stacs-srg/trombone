package uk.ac.standrews.cs.trombone.core.rpc;

import java.lang.reflect.Method;
import org.mashti.jetson.FutureResponse;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class FuturePeerResponse<Result> extends FutureResponse<Result> {

    private PeerReference correspondent;

    public FuturePeerResponse(final PeerReference correspondent, final Method method, final Object... arguments) {

        super(method, arguments);
        this.correspondent = correspondent;
    }

    public FuturePeerResponse(final Integer id) {

        super(id);
    }

    public PeerReference getCorrespondent() {

        return correspondent;
    }

    void setCorrespondent(final PeerReference correspondent) {

        this.correspondent = correspondent;
    }
}
