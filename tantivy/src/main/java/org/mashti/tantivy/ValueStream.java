package org.mashti.tantivy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class ValueStream {

    public static void main(String[] args) throws FileNotFoundException {

        File csv = null;
        BufferedReader reader = new BufferedReader(new FileReader(csv));
        AtomicLong sum = new AtomicLong();
        reader.lines()
                .map(line -> new CounterCsvRecord())
                .map(CounterCsvRecord:: getCount)
                .forEach(count -> sum.addAndGet(count));

    }
}
