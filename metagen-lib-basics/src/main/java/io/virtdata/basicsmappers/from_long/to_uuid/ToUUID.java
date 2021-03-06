package io.virtdata.basicsmappers.from_long.to_uuid;

import io.virtdata.api.ThreadSafeMapper;
import io.virtdata.basicsmappers.from_long.to_long.Hash;

import java.util.UUID;
import java.util.function.LongFunction;

/**
 * This function creates a non-random UUID in the type 4 version (Random).
 * It puts the same value in the MSB position of the UUID format.
 * The input value is put in the LSB position.
 * <pre>
 * xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
 * mmmmmmmm-mmmm-Mmmm-Llll-llllllllllll
 *               4    3
 * </pre>
 * As shown above, the LSB position does not have the complication of having
 * a version identifier (position M) dividing the dynamic range of the data type.
 * For this reason, only the LSB side is used for this mapper, which allows
 * an effective range of Long.MAX_VALUE/8, given the loss of 3 digits of precision.
 */
@ThreadSafeMapper
public class ToUUID implements LongFunction<UUID> {

    private final long msbs;
    private Hash longHash = new Hash();

    public ToUUID() {
        // Something memorable, but the correct version
        this.msbs = (0x0123456789ABCDEFL & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000004000L;
    }

    public ToUUID(long msbs) {
        this.msbs = (msbs & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000004000L;;
    }

    @Override
    public UUID apply(long value) {
        long lsbs = (value & 0x1FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        UUID uuid= new UUID(msbs,lsbs);
        return uuid;
    }
}
