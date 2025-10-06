/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wuwen5.hessian.io.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.github.wuwen5.hessian.io.SerializeTestBase;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class JavaTimeTest extends SerializeTestBase {

    @Test
    public void testDuration() throws Exception {
        List<Object> list = new ArrayList<>();
        Duration duration = Duration.ofDays(2);
        list.add(duration);
        list.add(duration);
        TestInner o = new TestInner();
        list.add(o);
        list.add(o);
        list.add(duration);
        list.add(o);
        List<Object> objects = baseHessian2Serialize(list);

        assertEquals(duration, objects.get(0));
        assertSame(objects.get(0), objects.get(1));

        assertListDuplicateReferences(Duration.ofDays(1));
    }

    @Test
    public void testInstant() throws Exception {
        Instant instant = Instant.now();
        Instant ret = baseHessian2Serialize(instant);
        assertEquals(ret, instant);

        assertListDuplicateReferences(Instant.now());
    }

    @Test
    public void testLocalTime() throws Exception {
        LocalTime localTime = LocalTime.now();
        LocalTime time = baseHessian2Serialize(localTime);
        assertEquals(time, localTime);
        assertListDuplicateReferences(LocalTime.now());
    }

    @Test
    public void testLocalDate() throws Exception {
        LocalDate localDate = LocalDate.now();
        LocalDate date = baseHessian2Serialize(localDate);
        assertEquals(date, localDate);
        assertListDuplicateReferences(LocalDate.now());
    }

    @Test
    public void testLocalDateTime() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime dateTime = baseHessian2Serialize(localDateTime);
        assertEquals(dateTime, localDateTime);
        assertListDuplicateReferences(LocalDateTime.now());
    }

    @Test
    public void testMonthDay() throws Exception {
        MonthDay monthDay = MonthDay.of(12, 25);
        MonthDay result = baseHessian2Serialize(monthDay);
        assertEquals(result, monthDay);
        assertListDuplicateReferences(MonthDay.now());
    }

    @Test
    public void testZoneOffset() throws Exception {
        ZoneOffset zoneOffset = ZoneOffset.ofHours(8);
        ZoneOffset result = baseHessian2Serialize(zoneOffset);
        assertEquals(result, zoneOffset);
        assertListDuplicateReferences(ZoneOffset.UTC);
    }

    @Test
    public void testOffsetTime() throws Exception {
        OffsetTime offsetTime = OffsetTime.now();
        OffsetTime result = baseHessian2Serialize(offsetTime);
        assertEquals(result, offsetTime);
        assertListDuplicateReferences(OffsetTime.now(ZoneOffset.UTC));
    }

    @Test
    public void testOffsetDateTime() throws Exception {
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        OffsetDateTime result = baseHessian2Serialize(offsetDateTime);
        assertEquals(result, offsetDateTime);
        assertListDuplicateReferences(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    public void testZonedDateTime() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        ZonedDateTime result = baseHessian2Serialize(zonedDateTime);
        assertEquals(result, zonedDateTime);
        assertListDuplicateReferences(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Test
    public void testYearMonth() throws Exception {
        YearMonth yearMonth = YearMonth.of(2023, 12);
        YearMonth result = baseHessian2Serialize(yearMonth);
        assertEquals(result, yearMonth);
        assertListDuplicateReferences(YearMonth.now());
    }

    @Test
    public void testYear() throws Exception {
        Year year = Year.of(2023);
        Year result = baseHessian2Serialize(year);
        assertEquals(result, year);
        assertListDuplicateReferences(Year.now());
    }

    @Test
    public void testPeriod() throws Exception {
        Period period = Period.of(1, 2, 3);
        Period result = baseHessian2Serialize(period);
        assertEquals(result, period);
        assertListDuplicateReferences(Period.ofDays(10));
    }

    @Test
    public void testZoneId() throws Exception {
        ZoneId zoneId = ZoneId.of("America/New_York");
        ZoneId result = baseHessian2Serialize(zoneId);
        assertEquals(result, zoneId);
        assertListDuplicateReferences(ZoneId.of("UTC"));
    }

    static class TestInner implements Serializable {
        String value;

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof TestInner) {
                return Objects.equals(((TestInner) o).value, value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }
    }
}
