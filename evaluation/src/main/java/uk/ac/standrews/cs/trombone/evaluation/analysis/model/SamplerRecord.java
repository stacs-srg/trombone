package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class SamplerRecord extends CounterRecord {

    private double min;
    private double mean;
    private double max;
    private double standardDeviation;
    private double percentileZeroPointOne;
    private double percentileOne;
    private double percentileTwo;
    private double percentileFive;
    private double percentileTwentyFive;
    private double percentileFifty;
    private double percentileSeventyFive;
    private double percentileNinetyFive;
    private double percentileNinetyEight;
    private double percentileNinetyNine;
    private double percentileNinetyNinePointNine;

    public double getStandardDeviation() {

        return standardDeviation;
    }

    public void setStandardDeviation(final double standardDeviation) {

        this.standardDeviation = standardDeviation;
    }

    public double getMax() {

        return max;
    }

    public void setMax(final double max) {

        this.max = max;
    }

    public double getMean() {

        return mean;
    }

    public void setMean(final double mean) {

        this.mean = mean;
    }

    public double getMin() {

        return min;
    }

    public void setMin(final double min) {

        this.min = min;
    }

    public double getPercentileZeroPointOne() {

        return percentileZeroPointOne;
    }

    public void setPercentileZeroPointOne(final double percentileZeroPointOne) {

        this.percentileZeroPointOne = percentileZeroPointOne;
    }

    public double getPercentileOne() {

        return percentileOne;
    }

    public void setPercentileOne(final double percentileOne) {

        this.percentileOne = percentileOne;
    }

    public double getPercentileTwo() {

        return percentileTwo;
    }

    public void setPercentileTwo(final double percentileTwo) {

        this.percentileTwo = percentileTwo;
    }

    public double getPercentileFive() {

        return percentileFive;
    }

    public void setPercentileFive(final double percentileFive) {

        this.percentileFive = percentileFive;
    }

    public double getPercentileTwentyFive() {

        return percentileTwentyFive;
    }

    public void setPercentileTwentyFive(final double percentileTwentyFive) {

        this.percentileTwentyFive = percentileTwentyFive;
    }

    public double getPercentileFifty() {

        return percentileFifty;
    }

    public void setPercentileFifty(final double percentileFifty) {

        this.percentileFifty = percentileFifty;
    }

    public double getPercentileSeventyFive() {

        return percentileSeventyFive;
    }

    public void setPercentileSeventyFive(final double percentileSeventyFive) {

        this.percentileSeventyFive = percentileSeventyFive;
    }

    public double getPercentileNinetyFive() {

        return percentileNinetyFive;
    }

    public void setPercentileNinetyFive(final double percentileNinetyFive) {

        this.percentileNinetyFive = percentileNinetyFive;
    }

    public double getPercentileNinetyEight() {

        return percentileNinetyEight;
    }

    public void setPercentileNinetyEight(final double percentileNinetyEight) {

        this.percentileNinetyEight = percentileNinetyEight;
    }

    public double getPercentileNinetyNine() {

        return percentileNinetyNine;
    }

    public void setPercentileNinetyNine(final double percentileNinetyNine) {

        this.percentileNinetyNine = percentileNinetyNine;
    }

    public double getPercentileNinetyNinePointNine() {

        return percentileNinetyNinePointNine;
    }

    public void setPercentileNinetyNinePointNine(final double percentileNinetyNinePointNine) {

        this.percentileNinetyNinePointNine = percentileNinetyNinePointNine;
    }
}
