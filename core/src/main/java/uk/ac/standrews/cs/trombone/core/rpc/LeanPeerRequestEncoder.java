package uk.ac.standrews.cs.trombone.core.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.LeanRequestEncoder;
import org.mashti.jetson.lean.codec.Codecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LeanPeerRequestEncoder extends LeanRequestEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeanPeerRequestEncoder.class);

    public LeanPeerRequestEncoder(final List<Method> dispatch, final Codecs codecs) {

        super(dispatch, codecs);
    }

    @Override
    protected void encode(final ChannelHandlerContext context, final FutureResponse<?> future_response, final ByteBuf out) {

        int current_index = out.writerIndex();
        try {
            addPendingFutureResponse(context, future_response);
            final Integer id = future_response.getId();
            final Method method = future_response.getMethod();
            final Object[] arguments = future_response.getArguments();
            final Type[] argument_types = method.getGenericParameterTypes();
            out.writeInt(id);
            writeCorrespondent(future_response, out);
            writeMethod(method, out);
            writeArguments(arguments, argument_types, out);
            future_response.notifyWrittenByteCount(out.writerIndex());
        }
        catch (final RPCException e) {
            future_response.notifyWrittenByteCount(out.writerIndex() - current_index);
            future_response.completeExceptionally(e);
        }
    }

    private void writeCorrespondent(final FutureResponse<?> future_response, final ByteBuf out) throws RPCException {

        final PeerReference correspondent;

        if (future_response instanceof FuturePeerResponse) {
            final FuturePeerResponse<?> future_peer_response = (FuturePeerResponse<?>) future_response;
            correspondent = future_peer_response.getCorrespondent();
        }
        else {
            correspondent = null;
            LOGGER.debug("no correspondent for {}", future_response);
        }

        codecs.encodeAs(correspondent, out, PeerReference.class);
    }
}
