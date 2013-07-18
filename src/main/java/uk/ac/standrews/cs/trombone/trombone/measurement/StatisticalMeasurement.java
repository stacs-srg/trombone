/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Trombone.
 *
 * Trombone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trombone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Trombone.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.trombone.trombone.measurement;

import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import uk.ac.standrews.cs.trombone.trombone.math.Statistics;
import uk.ac.standrews.cs.trombone.trombone.math.StatisticsStateless;

/**
 * Presents a statistical summary of a given {@link Statistics} instance.
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk);
 */
@Entity
@Table(name = "STATISTICAL_MEASUREMENTS")
@Access(AccessType.FIELD)
@NamedQuery(name = StatisticalMeasurement.FIND_BY_MEASURABLE_ASPECT_QUERY_NAME, query = "SELECT measurement FROM StatisticalMeasurement measurement WHERE measurement.measurable_aspect = :" + PersistableMeasurement.MEASURABLE_ASPECT_PARAMETER_NAME)
public class StatisticalMeasurement extends PersistableMeasurement {

    protected static final String FIND_BY_MEASURABLE_ASPECT_QUERY_NAME = "findStatisticalMeasurementByMeasurableAspect";
    private Number min;
    private Number max;
    private Number percentile_05;
    private Number percentile_10;
    private Number percentile_20;
    private Number percentile_30;
    private Number percentile_40;
    private Number percentile_50;
    private Number percentile_60;
    private Number percentile_70;
    private Number percentile_80;
    private Number percentile_90;
    private Number percentile_95;
    private Number sum;
    private Number sum_of_squares;
    private Number standard_deviation;
    private Number mean;
    private Long sample_size;

    /** The empty constructor required by JPA.*/
    protected StatisticalMeasurement() {

    }

    /**
     * Instantiates a new statistical summary.
     *
     * @param statistics the statistics
     * @param digest_type the digest type
     */
    public StatisticalMeasurement(final Statistics statistics, final MeasurableAspect digest_type) {

        super(digest_type);
        min = statistics.getMin();
        max = statistics.getMax();
        sum = statistics.getSum();
        sum_of_squares = statistics.getSumOfSquares();
        standard_deviation = statistics.getStandardDeviation();
        mean = statistics.getMean();
        sample_size = statistics.getSampleSize();
        initPercentiles(statistics);
    }

    public static List<StatisticalMeasurement> findByMeasurableAspect(final EntityManager entity_manager, final MeasurableAspect aspect) {

        final TypedQuery<StatisticalMeasurement> query = entity_manager.createNamedQuery(FIND_BY_MEASURABLE_ASPECT_QUERY_NAME, StatisticalMeasurement.class);
        query.setParameter(MEASURABLE_ASPECT_PARAMETER_NAME, aspect);
        return query.getResultList();
    }

    /**
     * Gets the sample size.
     *
     * @return the sample size
     */
    public Long getSampleSize() {

        return sample_size;
    }

    /**
     * Gets the standard deviation.
     *
     * @return the standard deviation
     */
    public Number getStandardDeviation() {

        return standard_deviation;
    }

    /**
     * Gets the mean.
     *
     * @return the mean
     */
    public Number getMean() {

        return mean;
    }

    /**
     * Gets the confidence interval.
     *
     * @param confidence_level the confidence_level
     * @return the confidence interval
     */
    public Number getConfidenceInterval(final Number confidence_level) {

        return StatisticsStateless.confidenceInterval(sample_size, standard_deviation, confidence_level);
    }

    /**
     * Gets the sum.
     *
     * @return the sum
     */
    public Number getSum() {

        return sum;
    }

    /**
     * Gets the sum of squares.
     *
     * @return the sum of squares
     */
    public Number getSumOfSquares() {

        return sum_of_squares;
    }

    private void initPercentiles(final Statistics statistics) {

        percentile_05 = statistics.getPercentile(5);
        percentile_10 = statistics.getPercentile(10);
        percentile_20 = statistics.getPercentile(20);
        percentile_30 = statistics.getPercentile(30);
        percentile_40 = statistics.getPercentile(40);
        percentile_50 = statistics.getPercentile(50);
        percentile_60 = statistics.getPercentile(60);
        percentile_70 = statistics.getPercentile(70);
        percentile_80 = statistics.getPercentile(80);
        percentile_90 = statistics.getPercentile(90);
        percentile_95 = statistics.getPercentile(95);
    }
}
