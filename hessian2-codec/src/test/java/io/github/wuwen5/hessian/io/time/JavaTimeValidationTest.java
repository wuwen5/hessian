package io.github.wuwen5.hessian.io.time;

import io.github.wuwen5.hessian.io.HessianDecoder;
import io.github.wuwen5.hessian.io.HessianEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.*;

/**
 * Manual validation test for Java Time API serialization
 */
public class JavaTimeValidationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Java Time API Serialization Validation ===\n");

        // Test LocalDateTime
        testType("LocalDateTime", LocalDateTime.of(2023, 12, 25, 10, 30, 45));

        // Test MonthDay
        testType("MonthDay", MonthDay.of(12, 25));

        // Test ZoneOffset
        testType("ZoneOffset", ZoneOffset.ofHours(8));

        // Test OffsetTime
        testType("OffsetTime", OffsetTime.of(10, 30, 45, 0, ZoneOffset.ofHours(8)));

        // Test OffsetDateTime
        testType("OffsetDateTime", OffsetDateTime.of(2023, 12, 25, 10, 30, 45, 0, ZoneOffset.ofHours(8)));

        // Test ZonedDateTime
        testType("ZonedDateTime", ZonedDateTime.of(2023, 12, 25, 10, 30, 45, 0, ZoneId.of("America/New_York")));

        // Test YearMonth
        testType("YearMonth", YearMonth.of(2023, 12));

        // Test Year
        testType("Year", Year.of(2023));

        // Test Period
        testType("Period", Period.of(1, 2, 3));

        // Test ZoneId
        testType("ZoneId", ZoneId.of("America/New_York"));

        // Test existing types
        testType("Duration", Duration.ofDays(2));
        testType("Instant", Instant.now());
        testType("LocalTime", LocalTime.of(10, 30, 45));
        testType("LocalDate", LocalDate.of(2023, 12, 25));

        System.out.println("\n=== All Java Time API types validated successfully! ===");
    }

    private static void testType(String name, Object value) throws Exception {
        // Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HessianEncoder encoder = new HessianEncoder(bos);
        encoder.writeObject(value);
        encoder.flush();

        byte[] data = bos.toByteArray();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        HessianDecoder decoder = new HessianDecoder(bis);
        Object result = decoder.readObject();

        // Verify
        boolean equals = value.equals(result);
        String status = equals ? "✓" : "✗";

        System.out.printf("%s %-20s : %s bytes, equals=%s%n", status, name, data.length, equals);

        if (!equals) {
            System.out.println("  Expected: " + value);
            System.out.println("  Got:      " + result);
            throw new AssertionError("Validation failed for " + name);
        }
    }
}
