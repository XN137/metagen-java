package io.virtdata.basicsmappers.from_long.to_int;

import io.virtdata.api.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class Add implements LongToIntFunction {

    private final long addend;

    public Add(long addend) {
        this.addend = addend;
    }

    @Override
    public int applyAsInt(long value) {
        return (int) ((value + addend) % Integer.MAX_VALUE);
    }
}
