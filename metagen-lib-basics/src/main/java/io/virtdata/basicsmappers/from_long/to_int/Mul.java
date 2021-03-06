package io.virtdata.basicsmappers.from_long.to_int;

import io.virtdata.api.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class Mul implements LongToIntFunction {

    public Mul(long multiplicand) {
        this.multiplicand = multiplicand;
    }

    private long multiplicand;

    @Override
    public int applyAsInt(long operand) {
        return (int) ((operand * multiplicand) % Integer.MAX_VALUE);
    }
}
