package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.churn.ConstantRateUncorrelatedChurn;
import uk.ac.standrews.cs.trombone.key.Key;
import uk.ac.standrews.cs.trombone.key.RandomIntegerKeyProvider;
import uk.ac.standrews.cs.trombone.math.ExponentialDistribution;
import uk.ac.standrews.cs.trombone.math.RandomNumberGenerator;
import uk.ac.standrews.cs.trombone.math.StatisticsStateless;
import uk.ac.standrews.cs.trombone.workload.ConstantRateWorkload;
import uk.ac.standrews.cs.trombone.workload.Workload;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerBehaviourGeneratorTest {

    private static final ExponentialDistribution first_arrival_delay_distribution = ExponentialDistribution.byMean(new Duration(10, TimeUnit.SECONDS));
    private static final ExponentialDistribution session_length_distribution = ExponentialDistribution.byMean(new Duration(10, TimeUnit.SECONDS));
    private static final ExponentialDistribution downtime_distribution = ExponentialDistribution.byMean(new Duration(10, TimeUnit.SECONDS));
    private static final ExponentialDistribution workload_intervals_distribution = ExponentialDistribution.byMean(new Duration(20, TimeUnit.MILLISECONDS));
    private static final Random uniform_random = new Random(55284);
    private static Duration experiemnt_duration = new Duration(60, TimeUnit.MINUTES);
    private Churn churn;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGeneration() throws Exception {

        long experiemnt_duration_nanos = experiemnt_duration.getLength(TimeUnit.NANOSECONDS);
        final File file = new File("/Users/masih/Desktop/churn.txt");
        final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        //        final PrintWriter out = new PrintWriter(file);
        final SortedSet<AvailabilityChangeEvent> availabilityChangeEvents = new TreeSet<AvailabilityChangeEvent>();
        for (int j = 0; j < 20; j++) {
            long experiment_time_nanos = 0;
            final List<Churn.Availability> availabilityList = new ArrayList<Churn.Availability>();
            final Duration first_arrival_delay = generateFirstArrivalDelay();
            churn = new ConstantRateUncorrelatedChurn(first_arrival_delay, session_length_distribution, downtime_distribution, uniform_random.nextLong());
            while (experiment_time_nanos < experiemnt_duration_nanos) {
                final Churn.Availability availability = churn.getAvailabilityAt(experiment_time_nanos);
                availabilityList.add(availability);
                final long duration = availability.getDuration().getLength(TimeUnit.NANOSECONDS);
                final boolean available = availability.isAvailable();

                final long i;
                out.writeByte(available ? 1 : 0);
                //                out.print(available);
                experiment_time_nanos += duration;
                if (experiment_time_nanos > experiemnt_duration_nanos) {
                    availabilityChangeEvents.add(new AvailabilityChangeEvent(j, false, experiemnt_duration_nanos));
                    i = experiemnt_duration_nanos - (experiment_time_nanos - duration);
                }
                else {
                    availabilityChangeEvents.add(new AvailabilityChangeEvent(j, available, experiment_time_nanos));
                    i = duration;
                }
                //                out.print(Long.toString(i));
                out.writeLong(i);

            }
            //            out.println();
            out.writeChar('\n');
            out.flush();
        }
        out.close();

        for (int i = 0; i < 20; i++) {
            Workload workload = new ConstantRateWorkload(workload_intervals_distribution, new RandomIntegerKeyProvider(uniform_random.nextLong()), 5, uniform_random.nextLong());

            for (AvailabilityChangeEvent event : availabilityChangeEvents) {

                if (event.available) {
                }
                final Integer peer = event.getSource();

            }

        }

        TreeMap<Long, List<Integer>> available_peers = new TreeMap<Long, List<Integer>>();
        TreeMap<Long, List<Integer>> available_peers2 = new TreeMap<Long, List<Integer>>();
        StatisticsStateless stats = new StatisticsStateless();

        Set<Integer> iii = new HashSet<Integer>();

        for (AvailabilityChangeEvent event : availabilityChangeEvents) {
            final Integer peer = (Integer) event.getSource();
            boolean changed;
            if (event.available) {
                changed = iii.add(peer);
                final Map.Entry<Long, List<Integer>> entry = available_peers.floorEntry(event.time);
                List<Integer> p;
                if (entry == null) {
                    p = new ArrayList<Integer>();
                    p.add(peer);
                }
                else {
                    entry.getValue().add(peer);
                    p = available_peers.remove(entry.getKey());
                }
                available_peers.put(event.time, p);
            }
            else {
                changed = iii.remove(peer);
                final Map.Entry<Long, List<Integer>> entry = available_peers.floorEntry(event.time);
                List<Integer> p;
                if (entry == null) {
                    p = new ArrayList<Integer>();

                }
                else {
                    p = new ArrayList<Integer>(entry.getValue());
                    p.remove(peer);

                }
                available_peers.put(event.time, p);
            }
            stats.addSample(iii.size());
            if (changed) {
                available_peers2.put(event.time, new ArrayList<Integer>(iii));

            }
        }

        System.out.println(stats);
        stats.reset();

        for (Map.Entry<Long, List<Integer>> entry : available_peers2.entrySet()) {
            stats.addSample(entry.getValue().size());
            //            System.out.println(entry.getKey() + " -> " + entry.getValue().toString());
        }
        System.out.println(stats);
        System.out.println(available_peers.size());

    }

    private Duration generateFirstArrivalDelay() {

        return RandomNumberGenerator.generateDurationInNanoseconds(first_arrival_delay_distribution, uniform_random);
    }

    class LookupEvent extends EventObject implements Comparable<LookupEvent> {

        private static final long serialVersionUID = -1151062831541968437L;
        private final Key target;
        private final Integer expectd_result;
        private final Long time;

        public LookupEvent(final Integer source, Key target, Integer expectd_result, Long time) {

            super(source);
            this.target = target;
            this.expectd_result = expectd_result;
            this.time = time;
        }

        @Override
        public Integer getSource() {

            return (Integer) super.getSource();
        }

        @Override
        public int compareTo(final LookupEvent other) {

            return time.compareTo(other.time);
        }
    }

    class AvailabilityChangeEvent extends EventObject implements Comparable<AvailabilityChangeEvent> {

        private final boolean available;
        private final Long time;

        public AvailabilityChangeEvent(final Integer source, boolean available, Long time) {

            super(source);
            this.available = available;
            this.time = time;
        }

        @Override
        public int compareTo(final AvailabilityChangeEvent other) {

            return time.compareTo(other.time);
        }

        @Override
        public Integer getSource() {

            return (Integer) super.getSource();
        }
    }

}
