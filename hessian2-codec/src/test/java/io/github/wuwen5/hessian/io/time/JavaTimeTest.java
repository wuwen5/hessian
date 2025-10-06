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
import java.time.LocalTime;
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
