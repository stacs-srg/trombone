package uk.ac.standrews.cs.trombone.math;

import org.apache.commons.math3.util.FastMath;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ZipfDistribution implements ProbabilityDistribution {

    private final Integer elements_count;
    private final double exponent;
    private final double harmonic_n_exp;
    private final double harmonic_n_exp_minus_1;

    public ZipfDistribution(Integer elements_count, double exponent) {

        NumericalRangeValidator.validateRangeLargerThanOneInclusive(elements_count);
        this.elements_count = elements_count;
        this.exponent = exponent;
        harmonic_n_exp = generalizedHarmonic(elements_count, exponent);
        harmonic_n_exp_minus_1 = generalizedHarmonic(elements_count, exponent - ONE);
    }

    @Override
    public Number probability(final Number x) {
        NumericalRangeValidator.validateRange(x, 1, elements_count, true, true);
        return (ONE / FastMath.pow(x.doubleValue(), exponent)) / harmonic_n_exp;
    }

    @Override
    public Number cumulative(final Number x) {
        NumericalRangeValidator.validateRange(x, ONE, elements_count, true, true);
        //TODO optimize
        return generalizedHarmonic(x.intValue(), exponent) / harmonic_n_exp;
    }

    @Override
    public Number quantile(final Number probability) {

        //TODO implement
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number mean() {
        return harmonic_n_exp_minus_1 / harmonic_n_exp;
    }

    @Override
    public Number median() {
        //TODO implement
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number mode() {
        return ONE;
    }

    @Override
    public Number variance() {
        //TODO implement according to : http://mathworld.wolfram.com/ZipfDistribution.html
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number skewness() {
        return exponent;
    }

    /**
     * Calculates the Nth generalized harmonic number. See
     * <a href="http://mathworld.wolfram.com/HarmonicSeries.html">Harmonic
     * Series</a>.
     *
     * @param n Term in the series to calculate (must be larger than 1)
     * @param m Exponent (special case {@code m = 1} is the harmonic series).
     * @return the n<sup>th</sup> generalized harmonic number.
     */
    static double generalizedHarmonic(final int n, final double m) {
        double value = 0;
        for (int k = n; k > 0; --k) {
            value += 1.0 / Math.pow(k, m);
        }
        return value;
    }
}
