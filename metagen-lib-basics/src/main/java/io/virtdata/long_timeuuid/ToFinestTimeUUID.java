package io.virtdata.long_timeuuid;

import io.virtdata.api.Example;
import io.virtdata.long_long.HostHash;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.LongFunction;

/**
 * Converts a count of 100ns intervals from 1582 Julian to a Type1 TimeUUID
 * according to <a href="https://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
 * This allows you to access the finest unit of resolution for the
 * purposes of simulating a large set of unique timeuuid values. This offers
 * 10000 times more unique values per ms than {@link ToEpochTimeUUID}.
 * <p>
 * For the variants that have an String argument in the constructor, this is
 * a parsable datetime that is used as the base time for all produced values.
 * Setting this allows you to set the start of the time range for all timeuuid
 * values produced. All times are parsed for UTC. All time use ISO date ordering,
 * meaning that the most significant fields always go before the others.
 * <p>
 * The valid formats, in joda specifier form are:
 * <ol>
 * <li>yyyy-MM-dd HH:mm:ss.SSSZ, for example: 2015-02-28 23:30:15.223</li>
 * <li>yyyy-MM-dd HH:mm:ss, for example 2015-02-28 23:30:15</li>
 * <li>yyyyMMdd'T'HHmmss.SSSZ, for example: 20150228T233015.223</li>
 * <li>yyyyMMdd'T'HHmmssZ, for example: 20150228T233015</li>
 * <li>yyyy-MM-dd, for example: 2015-02-28</li>
 * <li>yyyyMMdd, for example: 20150228</li>
 * <li>yyyyMM, for example: 201502</li>
 * <li>yyyy, for example: 2015</li>
 * </ol>
 */
@Example("ToFinestTimeUUID() // basetime 0, computed node data, empty clock data")
@Example("ToFinestTimeUUID(5234) // basetime 0, specified node data (5234), empty clock data")
@Example("ToFinestTimeUUID(31,337) // basetime 0, specified node data (31) and clock data (337)")
@Example("ToFinestTimeUUID('2017-01-01T23:59:59') // specified basetime, computed node data, empty clock data")
@Example("ToFinestTimeUUID('2012',12345) // basetime at start if 2012, with node data 12345, empty clock data")
@Example("ToFinestTimeUUID('20171231T1015.243',123,456) // ms basetime, specified node and clock data")
public class ToFinestTimeUUID implements LongFunction<UUID> {

    private final static DateTimeFormatter[] formatters = new DateTimeFormatter[]{
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
                    .withChronology(GregorianChronology.getInstance()),
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                    .withChronology(GregorianChronology.getInstance()),
            ISODateTimeFormat.basicDateTime()
                    .withChronology(GregorianChronology.getInstance()), // yyyyMMdd'T'HHmmss.SSSZ
            ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC()
                    .withChronology(GregorianChronology.getInstance()), // yyyyMMdd'T'HHmmssZ
            ISODateTimeFormat.date().withZoneUTC()
                    .withChronology(GregorianChronology.getInstance()), // yyyy-MM-dd
            DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC()
                    .withChronology(GregorianChronology.getInstance()),
            DateTimeFormat.forPattern("yyyyMM").withZoneUTC()
                    .withChronology(GregorianChronology.getInstance()),
            DateTimeFormat.forPattern("yyyy").withZoneUTC()
                    .withChronology(GregorianChronology.getInstance())
    };

    private final long node;
    private final long clock;
    private final long baseTicks;

    /**
     * Create version 1 timeuuids with a per-host node and empty clock data.
     * The node and clock components are seeded from network interface data. In this case,
     * the clock data is not seeded uniquely.
     */
    public ToFinestTimeUUID() {
        this.node = new HostHash().applyAsLong(1);
        this.clock = 0L;
        this.baseTicks = 0L;
    }

    /**
     * Create version 1 timeuuids with a specific static node and empty clock data.
     * This is useful for testing so that you can know that values are verifiable, even though
     * in non-testing practice, you would rely on some form of entropy per-system to provide
     * more practical dispersion of values over reboots, etc.
     *
     * @param node a fixture value for testing that replaces node and clock bits
     */
    public ToFinestTimeUUID(long node) {
        this.node = node;
        this.clock = 0L;
        this.baseTicks = 0L;
    }

    /**
     * Create version 1 timeuuids with a specific static node and specific clock data.
     * This is useful for testing so that you can know that values are verifiable, even though
     * in non-testing practice, you would rely on some form of entropy per-system to provide
     * more practical dispersion of values over reboots, etc.
     *
     * @param node  a fixture value for testing that replaces node bits
     * @param clock a fixture value for testing that replaces clock bits
     */
    public ToFinestTimeUUID(long node, long clock) {
        this.node = node;
        this.clock = clock;
        this.baseTicks = 0L;
    }

    /**
     * Create version 1 timeuuids with a per-host node and empty clock data.
     * The node and clock components are seeded from network interface data. In this case,
     * the clock data is not seeded uniquely.
     * @param baseTimeSpec - a string specification for the base time value
     */
    public ToFinestTimeUUID(String baseTimeSpec) {
        this.node = new HostHash().applyAsLong(1);
        this.clock = 0L;
        this.baseTicks = parseBaseTimeTicks(baseTimeSpec);
    }

    /**
     * Create version 1 timeuuids with a specific static node and empty clock data.
     * This is useful for testing so that you can know that values are verifiable, even though
     * in non-testing practice, you would rely on some form of entropy per-system to provide
     * more practical dispersion of values over reboots, etc.
     *
     * @param baseTimeSpec - a string specification for the base time value
     * @param node a fixture value for testing that replaces node and clock bits
     */
    public ToFinestTimeUUID(String baseTimeSpec, long node) {
        this.node = node;
        this.clock = 0L;
        this.baseTicks = parseBaseTimeTicks(baseTimeSpec);
    }

    /**
     * Create version 1 timeuuids with a specific static node and specific clock data.
     * This is useful for testing so that you can know that values are verifiable, even though
     * in non-testing practice, you would rely on some form of entropy per-system to provide
     * more practical dispersion of values over reboots, etc.
     *
     * @param node  a fixture value for testing that replaces node bits
     * @param clock a fixture value for testing that replaces clock bits
     * @param baseTimeSpec - a string specification for the base time value
     */
    public ToFinestTimeUUID(String baseTimeSpec, long node, long clock) {
        this.node = node;
        this.clock = clock;
        this.baseTicks = parseBaseTimeTicks(baseTimeSpec);
    }

    private static long msbBitsFor(long timeClicks) {
        return 0x0000000000001000L
                | (0x0fff000000000000L & timeClicks) >>> 48
                | (0x0000ffff00000000L & timeClicks) >>> 16
                | (0x00000000ffffffffL & timeClicks) << 32;
    }

    private static long lsbBitsFor(long node, long clock) {
        return ((clock & 0x0000000000003FFFL) << 48) | 0x8000000000000000L | node;
    }

    @Override
    public UUID apply(long timeClicks) {
        return new UUID(msbBitsFor(timeClicks + baseTicks), lsbBitsFor(node, clock));
    }


    private long parseBaseTimeTicks(String timeString) {
        DateTime gregsCalendar = new DateTime(1582,10,15,0,0, DateTimeZone.UTC);

        List<Exception> exceptions = new ArrayList<>();
        for (DateTimeFormatter dtf : formatters) {
            try {
                DateTime dateTime = dtf.withZoneUTC().parseDateTime(timeString);
                long ticks = new Duration(gregsCalendar,dateTime).getMillis() * 10000;
                return ticks;
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        String message = "";
        for (Exception e : exceptions) {
            message += e.getMessage() + "\n";
        }
        throw new RuntimeException("Unable to parse [" + timeString + "] with any of the parsers. exceptions:" + message);
    }

}