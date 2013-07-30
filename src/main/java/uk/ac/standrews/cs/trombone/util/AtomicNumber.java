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
package uk.ac.standrews.cs.trombone.util;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicNumber extends Number {

    private final AtomicLong long_bits;

    public AtomicNumber() {

        this(0.0D);
    }

    public AtomicNumber(final Number initial_value) {

        long_bits = new AtomicLong(toLongBits(initial_value));
    }

    @Override
    public int intValue() {

        return (int) doubleValue();
    }

    @Override
    public long longValue() {

        return (long) doubleValue();
    }

    @Override
    public float floatValue() {

        return (float) doubleValue();
    }

    @Override
    public double doubleValue() {

        return Double.longBitsToDouble(long_bits.get());
    }

    @Override
    public String toString() {

        return String.valueOf(get());
    }

    public Number get() {

        return fromLongBits(long_bits.get());
    }

    public void set(final Number value) {

        long_bits.set(toLongBits(value));
    }

    public Number getAndSet(final Number value) {

        return fromLongBits(long_bits.getAndSet(toLongBits(value)));
    }

    public boolean compareAndSet(final Number expect, final Number update) {

        return long_bits.compareAndSet(toLongBits(expect), toLongBits(update));
    }

    public boolean weakCompareAndSet(final Number expect, final Number update) {

        return long_bits.weakCompareAndSet(toLongBits(expect), toLongBits(update));
    }

    public Number addAndGet(final Number value) {

        long current;
        long next;
        do {
            current = long_bits.get();
            next = toLongBits(fromLongBits(current) + value.doubleValue());
        }
        while (!long_bits.compareAndSet(current, next));
        return fromLongBits(next);
    }

    public Number setIfGreaterAndGet(final Number value) {

        long current;
        long next;
        do {
            current = long_bits.get();
            next = toLongBits(Math.max(fromLongBits(current), value.doubleValue()));
        }
        while (!long_bits.compareAndSet(current, next));
        return fromLongBits(next);
    }

    public Number setIfSmallerAndGet(final Number value) {

        long current;
        long next;
        do {
            current = long_bits.get();
            next = toLongBits(Math.min(fromLongBits(current), value.doubleValue()));
        }
        while (!long_bits.compareAndSet(current, next));
        return fromLongBits(next);
    }

    private long toLongBits(final Number value) {

        return Double.doubleToRawLongBits(value.doubleValue());
    }

    private double fromLongBits(final long value) {

        return Double.longBitsToDouble(value);
    }
}
