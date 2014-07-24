package uk.ac.standrews.cs.trombone.core.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.util.List;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.LeanRequestDecoder;
import org.mashti.jetson.lean.codec.Codecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LeanPeerRequestDecoder extends LeanRequestDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeanPeerRequestDecoder.class);

    public LeanPeerRequestDecoder(final List<Method> dispatch, final Codecs codecs) {

        super(dispatch, codecs);
    }

    @Override
    protected FutureResponse<?> decode(final ChannelHandlerContext context, final ByteBuf in) {

        FuturePeerResponse<?> future_response = null;
        final Integer id;
        final PeerReference correspondent;
        final Method method;
        final Object[] arguments;
        try {
            beforeDecode(context, in);
            
            id = decodeId(context, in);
            future_response = new FuturePeerResponse<>(id);

            correspondent = codecs.decodeAs(in, PeerReference.class);
            future_response.setCorrespondent(correspondent);

            method = decodeMethod(context, in);
            future_response.setMethod(method);

            arguments = decodeMethodArguments(context, in, method);
            future_response.setArguments(arguments);
        }
        catch (RPCException e) {
            LOGGER.warn("error decoding request", e);

            if (future_response != null) {
                future_response.completeExceptionally(e);
            }
            else {
                LOGGER.warn("cannot handle bad request", e);
            }
        }
        finally {
            afterDecode(context, in);
        }
        return future_response;
    }

}
