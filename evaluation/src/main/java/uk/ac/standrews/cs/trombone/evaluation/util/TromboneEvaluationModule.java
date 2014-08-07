package uk.ac.standrews.cs.trombone.evaluation.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;
import uk.ac.standrews.cs.trombone.core.maintenance.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.state.PeerStateFactory;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.IntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
class TromboneEvaluationModule extends SimpleModule {

    private static final long serialVersionUID = 5427903503246027975L;

    TromboneEvaluationModule() {

        super("TromboneEvaluationModule");
        setMixInAnnotation(Supplier.class, Mixin.class);
        setMixInAnnotation(JoinStrategy.class, Mixin.class);
        setMixInAnnotation(LookupStrategy.class, Mixin.class);
        setMixInAnnotation(NextHopStrategy.class, Mixin.class);
        setMixInAnnotation(SyntheticDelay.class, Mixin.class);
        setMixInAnnotation(MaintenanceFactory.class, Mixin.class);
        setMixInAnnotation(PeerStateFactory.class, Mixin.class);
        setMixInAnnotation(ExecutorService.class, Mixin.class);
        setMixInAnnotation(Workload.class, Mixin.class);
        setMixInAnnotation(Churn.class, Mixin.class);
        setMixInAnnotation(IntervalGenerator.class, Mixin.class);
        setMixInAnnotation(RejectedExecutionHandler.class, Mixin.class);
        setMixInAnnotation(ThreadFactory.class, Mixin.class);
        setMixInAnnotation(PeerConfiguration.class, Mixin.class);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonPropertyOrder(alphabetic = true)
    private interface Mixin {}
}
