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

package io.github.wuwen5.hessian.io;

import static io.github.wuwen5.hessian.io.Hessian2Constants.BC_DOUBLE_BYTE;
import static io.github.wuwen5.hessian.io.Hessian2Constants.BC_DOUBLE_MILL;
import static io.github.wuwen5.hessian.io.Hessian2Constants.BC_DOUBLE_SHORT;
import static io.github.wuwen5.hessian.io.Hessian2Constants.BC_INT_BYTE_ZERO;
import static io.github.wuwen5.hessian.io.Hessian2Constants.BC_LONG_INT;
import static io.github.wuwen5.hessian.io.Hessian2Constants.INT_BYTE_MAX;
import static io.github.wuwen5.hessian.io.Hessian2Constants.INT_BYTE_MIN;
import static io.github.wuwen5.hessian.io.Hessian2Constants.INT_DIRECT_MIN;
import static io.github.wuwen5.hessian.io.Hessian2Constants.INT_SHORT_MAX;
import static io.github.wuwen5.hessian.io.Hessian2Constants.INT_SHORT_MIN;
import static io.github.wuwen5.hessian.io.Hessian2Constants.LONG_BYTE_MAX;
import static io.github.wuwen5.hessian.io.Hessian2Constants.STRING_SHORT_MAX;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.github.wuwen5.hessian.io.beans.BaseUser;
import io.github.wuwen5.hessian.io.beans.black.DenyObj;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HessianIOTest extends SerializeTestBase {

    @Test
    public void testHession2Init() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Hessian2Output output = new Hessian2Output()) {
            output.init(outputStream);
            output.writeBoolean(true);
        }
        byte[] byteArray = outputStream.toByteArray();

        try (Hessian2Input input = new Hessian2Input()) {
            input.init(new ByteArrayInputStream(byteArray));
            assertTrue(input.readBoolean());
            assertThrows(HessianProtocolException.class, input::readNull);
        }
    }

    @Test
    public void testReadBoolean() throws IOException {

        Boolean ret = hessianIO(output -> Try.run(() -> output.writeBoolean(true)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(false)), input -> Try.of(input::readBoolean)
                .get());
        assertFalse(ret);

        ret = hessianIO(output -> Try.run(output::writeNull), input -> Try.of(input::readBoolean)
                .get());
        assertFalse(ret);
        ret = hessianIO(output -> Try.run(() -> output.writeInt(12345678)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);
        ret = hessianIO(output -> Try.run(() -> output.writeInt(0)), input -> Try.of(input::readBoolean)
                .get());
        assertFalse(ret);

        ret = hessianIO(output -> Try.run(() -> output.writeLong(121111111131L)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);
        ret = hessianIO(output -> Try.run(() -> output.writeLong(0L)), input -> Try.of(input::readBoolean)
                .get());
        assertFalse(ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(0L)), input -> Try.of(input::readBoolean)
                .get());
        assertFalse(ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1L)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(-0x8000)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(-0x80)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1.12312313)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1.123)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);
        Integer i = hessianIO(
                output -> Try.run(() -> {
                    output.writeDouble(1.123);
                }),
                input -> Try.of(input::read).get());
        assertEquals(BC_DOUBLE_MILL, i);

        ret = hessianIO(output -> Try.run(() -> output.writeInt(INT_BYTE_MAX)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        // 0xc8, BC_INT_BYTE_ZERO + 0
        ret = hessianIO(output -> Try.run(() -> output.writeInt(48)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        // BC_INT_SHORT_ZERO + 0
        ret = hessianIO(output -> Try.run(() -> output.writeInt(3000)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        // 0xf8, BC_LONG_BYTE_ZERO + 0
        ret = hessianIO(output -> Try.run(() -> output.writeLong(48)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        // LONG_SHORT != 0
        ret = hessianIO(output -> Try.run(() -> output.writeLong(-262144)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        // BC_LONG_SHORT_ZERO
        ret = hessianIO(output -> Try.run(() -> output.writeLong(65535)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        ret = hessianIO(
                output -> Try.run(() -> output.writeLong(Integer.MAX_VALUE)),
                input -> Try.of(input::readBoolean).get());
        assertTrue(ret);
        assertEquals(
                (byte) (BC_LONG_INT),
                hessianIO(output -> Try.run(() -> output.writeLong(Integer.MAX_VALUE)), input -> Try.of(input::read)
                                .get())
                        .byteValue());

        assertEquals(
                (byte) (BC_INT_BYTE_ZERO + (INT_BYTE_MAX >> 8)),
                hessianIO(output -> Try.run(() -> output.writeInt(INT_BYTE_MAX)), input -> Try.of(input::read)
                                .get())
                        .byteValue());

        ret = hessianIO(output -> Try.run(() -> output.writeInt(INT_SHORT_MAX)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);

        ret = hessianIO(output -> Try.run(() -> output.writeLong(LONG_BYTE_MAX)), input -> Try.of(input::readBoolean)
                .get());
        assertTrue(ret);
    }

    @Test
    public void testReadInt() throws IOException {
        // Test integer value 12345
        Integer ret = hessianIO(output -> Try.run(() -> output.writeInt(12345)), input -> Try.of(input::readInt)
                .get());
        assertEquals(12345, ret);

        ret = hessianIO(output -> Try.run(output::writeNull), input -> Try.of(input::readInt)
                .get());
        assertEquals(0, ret);
        short ret2 = hessianIO(output -> Try.run(output::writeNull), input -> Try.of(input::readShort)
                .get());
        assertEquals(0, ret2);

        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(false)), input -> Try.of(input::readInt)
                .get());
        assertEquals(0, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(true)), input -> Try.of(input::readInt)
                .get());
        assertEquals(1, ret);

        // LONG_DIRECT_MAX
        ret = hessianIO(output -> Try.run(() -> output.writeLong(15)), input -> Try.of(input::readInt)
                .get());
        assertEquals(15, ret);
        // LONG_BYTE_MAX
        ret = hessianIO(output -> Try.run(() -> output.writeLong(2047)), input -> Try.of(input::readInt)
                .get());
        assertEquals(2047, ret);
        // LONG_SHORT_MAX
        ret = hessianIO(output -> Try.run(() -> output.writeLong(262143)), input -> Try.of(input::readInt)
                .get());
        assertEquals(262143, ret);

        ret = hessianIO(output -> Try.run(() -> output.writeLong(3000000000L)), input -> Try.of(input::readInt)
                .get());
        assertEquals((int) 3000000000L, ret);

        ret = hessianIO(output -> Try.run(() -> output.writeDouble(0)), input -> Try.of(input::readInt)
                .get());
        assertEquals(0, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1)), input -> Try.of(input::readInt)
                .get());
        assertEquals(1, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(-0x80)), input -> Try.of(input::readInt)
                .get());
        assertEquals(-0x80, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(-0x80)), input -> Try.of(input::read)
                .get());
        assertEquals(BC_DOUBLE_BYTE, ret);

        // BC_DOUBLE_SHORT
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(3000)), input -> Try.of(input::readInt)
                .get());
        assertEquals(3000, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(3000)), input -> Try.of(input::read)
                .get());
        assertEquals(BC_DOUBLE_SHORT, ret);

        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1.123)), input -> Try.of(input::readInt)
                .get());
        assertEquals(1, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1.123456)), input -> Try.of(input::readInt)
                .get());
        assertEquals(1, ret);
    }

    @Test
    public void testReadLong() throws IOException {
        // Test long value 123456789
        Long ret = hessianIO(output -> Try.run(() -> output.writeLong(123456789)), input -> Try.of(input::readLong)
                .get());
        assertEquals(123456789L, ret);

        ret = hessianIO(output -> Try.run(output::writeNull), input -> Try.of(input::readLong)
                .get());
        assertEquals(0, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(false)), input -> Try.of(input::readLong)
                .get());
        assertEquals(0, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(true)), input -> Try.of(input::readLong)
                .get());
        assertEquals(1, ret);

        // ----------
        ret = hessianIO(output -> Try.run(() -> output.writeInt(15)), input -> Try.of(input::readLong)
                .get());
        assertEquals(15, ret);
        // INT_BYTE_MAX
        ret = hessianIO(output -> Try.run(() -> output.writeInt(2047)), input -> Try.of(input::readLong)
                .get());
        assertEquals(2047, ret);
        // byte long
        ret = (Long) hessianIO(output -> Try.run(() -> output.writeLong(2047)), input -> Try.of(input::readObject)
                .get());
        assertEquals(2047, ret);
        // INT_SHORT_MAX
        ret = hessianIO(output -> Try.run(() -> output.writeInt(262143)), input -> Try.of(input::readLong)
                .get());
        assertEquals(262143, ret);
        // shot long
        ret = (Long) hessianIO(output -> Try.run(() -> output.writeLong(262143)), input -> Try.of(input::readObject)
                .get());
        assertEquals(262143, ret);
        // BC_LONG_INT
        ret = (Long) hessianIO(
                output -> Try.run(() -> output.writeLong(Integer.MAX_VALUE)),
                input -> Try.of(input::readObject).get());
        assertEquals(Integer.MAX_VALUE, ret);
        // BC_LONG
        ret = (Long)
                hessianIO(output -> Try.run(() -> output.writeLong(Long.MAX_VALUE)), input -> Try.of(input::readObject)
                        .get());
        assertEquals(Long.MAX_VALUE, ret);

        ret = hessianIO(output -> Try.run(() -> output.writeLong(3000000000L)), input -> Try.of(input::readLong)
                .get());
        assertEquals(3000000000L, ret);

        ret = (Long)
                hessianIO(output -> Try.run(() -> output.writeLong(3000000000L)), input -> Try.of(input::readObject)
                        .get());
        assertEquals(3000000000L, ret);

        ret = hessianIO(output -> Try.run(() -> output.writeDouble(0)), input -> Try.of(input::readLong)
                .get());
        assertEquals(0, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1)), input -> Try.of(input::readLong)
                .get());
        assertEquals(1, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(-0x80)), input -> Try.of(input::readLong)
                .get());
        assertEquals(-0x80, ret);

        // BC_DOUBLE_SHORT
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(3000)), input -> Try.of(input::readLong)
                .get());
        assertEquals(3000, ret);

        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1.123)), input -> Try.of(input::readLong)
                .get());
        assertEquals(1, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1.123456)), input -> Try.of(input::readLong)
                .get());
        assertEquals(1, ret);

        // write double read object
        Double dret =
                (Double) hessianIO(output -> Try.run(() -> output.writeDouble(0)), input -> Try.of(input::readObject)
                        .get());
        assertEquals(0.0, dret);
        dret = (Double) hessianIO(output -> Try.run(() -> output.writeDouble(1)), input -> Try.of(input::readObject)
                .get());
        assertEquals(1.0, dret);
        dret = (Double) hessianIO(output -> Try.run(() -> output.writeDouble(-0x80)), input -> Try.of(input::readObject)
                .get());
        assertEquals(-0x80, dret);

        // BC_DOUBLE_SHORT
        dret = (Double) hessianIO(output -> Try.run(() -> output.writeDouble(3000)), input -> Try.of(input::readObject)
                .get());
        assertEquals(3000, dret);

        dret = (Double) hessianIO(output -> Try.run(() -> output.writeDouble(1.123)), input -> Try.of(input::readObject)
                .get());
        assertEquals(1.123, dret);
        dret = (Double)
                hessianIO(output -> Try.run(() -> output.writeDouble(1.123456)), input -> Try.of(input::readObject)
                        .get());
        assertEquals(1.123456, dret);

        // BC_LONG_BYTE
        ret = hessianIO(output -> Try.run(() -> output.writeLong(48)), input -> Try.of(input::readLong)
                .get());
        assertEquals(48, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeLong(262143)), input -> Try.of(input::readLong)
                .get());
        assertEquals(262143, ret);
    }

    @Test
    public void testReadDouble() throws IOException {
        // Test double value 3.14159
        Double ret = hessianIO(output -> Try.run(() -> output.writeDouble(3.14159)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(3.14159, ret, 0.00001);

        ret = hessianIO(output -> Try.run(output::writeNull), input -> Try.of(input::readDouble)
                .get());
        assertEquals(0, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(false)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(0, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(true)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(1, ret);

        Float ret1 = hessianIO(output -> Try.run(() -> output.writeDouble(1.0f)), input -> Try.of(input::readFloat)
                .get());
        assertEquals(1f, ret1);

        ret = hessianIO(output -> Try.run(() -> output.writeInt(15)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(15, ret);

        ret = hessianIO(output -> Try.run(() -> output.writeInt(2047)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(2047, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeInt(262143)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(262143, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeInt(Integer.MAX_VALUE)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(Integer.MAX_VALUE, ret);

        // direct long
        ret = hessianIO(output -> Try.run(() -> output.writeLong(15)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(15, ret);
        // byte long
        ret = hessianIO(output -> Try.run(() -> output.writeLong(2047)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(2047, ret);
        // short long
        ret = hessianIO(output -> Try.run(() -> output.writeLong(262143)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(262143, ret);
        // BC_LONG
        ret = hessianIO(output -> Try.run(() -> output.writeLong(Long.MAX_VALUE)), input -> Try.of(input::readDouble)
                .get());
        assertEquals(Long.MAX_VALUE, ret);
    }

    @Test
    public void testReadString() throws IOException {
        // Test string "hello"
        String s = hessianIO(output -> Try.run(() -> output.writeString("hello")), input -> Try.of(input::readString)
                .get());
        String none = hessianIO(output -> Try.run(() -> output.writeString(null)), input -> Try.of(input::readString)
                .get());
        String t = hessianIO(output -> Try.run(() -> output.writeBoolean(true)), input -> Try.of(input::readString)
                .get());
        String f = hessianIO(output -> Try.run(() -> output.writeBoolean(false)), input -> Try.of(input::readString)
                .get());
        String id =
                hessianIO(output -> Try.run(() -> output.writeInt(INT_DIRECT_MIN)), input -> Try.of(input::readString)
                        .get());
        String ib = hessianIO(output -> Try.run(() -> output.writeInt(INT_BYTE_MIN)), input -> Try.of(input::readString)
                .get());
        String is =
                hessianIO(output -> Try.run(() -> output.writeInt(INT_SHORT_MIN)), input -> Try.of(input::readString)
                        .get());
        String imax = hessianIO(
                output -> Try.run(() -> output.writeInt(Integer.MAX_VALUE)),
                input -> Try.of(input::readString).get());
        assertEquals("hello", s);
        assertNull(none);
        assertEquals("true", t);
        assertEquals("false", f);
        assertEquals("" + INT_DIRECT_MIN, id);
        assertEquals("" + INT_BYTE_MIN, ib);
        assertEquals("" + INT_SHORT_MIN, is);
        assertEquals("" + Integer.MAX_VALUE, imax);

        // direct long
        String ret = hessianIO(output -> Try.run(() -> output.writeLong(15)), input -> Try.of(input::readString)
                .get());
        assertEquals("15", ret);
        // byte long
        ret = hessianIO(output -> Try.run(() -> output.writeLong(2047)), input -> Try.of(input::readString)
                .get());
        assertEquals("2047", ret);
        // short long
        ret = hessianIO(output -> Try.run(() -> output.writeLong(262143)), input -> Try.of(input::readString)
                .get());
        assertEquals("262143", ret);
        // BC_LONG
        ret = hessianIO(output -> Try.run(() -> output.writeLong(Long.MAX_VALUE)), input -> Try.of(input::readString)
                .get());
        assertEquals(Long.MAX_VALUE + "", ret);

        ret = hessianIO(output -> Try.run(() -> output.writeDouble(0)), input -> Try.of(input::readString)
                .get());
        assertEquals("0.0", ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1)), input -> Try.of(input::readString)
                .get());
        assertEquals("1.0", ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(-0x80)), input -> Try.of(input::readString)
                .get());
        assertEquals(-0x80 + "", ret);

        // BC_DOUBLE_SHORT
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(3000)), input -> Try.of(input::readString)
                .get());
        assertEquals("3000", ret);

        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1.123)), input -> Try.of(input::readString)
                .get());
        assertEquals("1.123", ret);
        ret = hessianIO(output -> Try.run(() -> output.writeDouble(1.123456)), input -> Try.of(input::readString)
                .get());
        assertEquals("1.123456", ret);

        final String maxString = "x".repeat(STRING_SHORT_MAX + 1);
        ret = hessianIO(output -> Try.run(() -> output.writeString(maxString)), input -> Try.of(input::readString)
                .get());
        assertEquals(maxString, ret);

        ret = hessianIO(output -> Try.run(() -> output.writeString(maxString)), input -> Try.of(input::readObject)
                        .get())
                .toString();
        assertEquals(maxString, ret);
    }

    @Test
    public void testReadUTCDate() throws IOException {
        // Test date
        long now = System.currentTimeMillis();

        Long l = hessianIO(output -> Try.run(() -> output.writeUTCDate(now)), input -> Try.of(input::readUTCDate)
                .get());
        Date o = (Date)
                hessianIO(output -> Try.run(() -> output.writeObject(new Date(now))), input -> Try.of(input::readObject)
                        .get());

        // truncate minutes
        Instant instant = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Date date = Date.from(instant);
        long timestamp = date.getTime();
        Date dm = (Date) hessianIO(
                output -> Try.run(() -> output.writeObject(new Date(timestamp))),
                input -> Try.of(input::readObject).get());
        long utcTime = hessianIO(
                output -> Try.run(() -> output.writeObject(new Date(timestamp))),
                input -> Try.of(input::readUTCDate).get());

        java.sql.Date sqlDate = (java.sql.Date) hessianIO(
                output -> Try.run(() -> output.writeObject(new java.sql.Date(now))),
                input -> Try.of(input::readObject).get());
        assertEquals(now, l);
        assertEquals(now, o.getTime());
        assertEquals(now, sqlDate.getTime());
        assertEquals(timestamp, dm.getTime());
        assertEquals(timestamp, utcTime);
    }

    @Test
    public void testReadNull() throws IOException {
        byte[] nullData = {'N'};
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(nullData));
        input.readNull(); // Should not throw exception

        // Verify reading null returns null for objects
        input = new Hessian2Input(new ByteArrayInputStream(nullData));

        Object o = hessianIO(output -> Try.run(() -> output.writeObject(null)), i -> Try.of(i::readObject)
                .get());
        Object o2 = hessianIO(
                output -> Try.run(output::writeNull), i -> Try.run(i::readNull).get());

        assertNull(input.readObject());
        assertNull(o);
        assertNull(o2);
    }

    @Test
    public void testReadBytes() throws IOException {
        // Test byte array
        byte[] byteData = {'B', 0, 3, 1, 2, 3};
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(byteData));
        byte[] result = input.readBytes();

        byte[] bytes =
                hessianIO(output -> Try.run(() -> output.writeBytes(new byte[] {1, 2, 3})), i -> Try.of(i::readBytes)
                        .get());

        assertArrayEquals(new byte[] {1, 2, 3}, result);
        assertArrayEquals(new byte[] {1, 2, 3}, bytes);

        bytes = hessianIO(
                output -> Try.run(output::writeNull), i -> Try.of(i::readBytes).get());
        assertNull(bytes);

        bytes = hessianIO(output -> Try.run(() -> output.writeBytes(null)), i -> Try.of(i::readBytes)
                .get());
        assertNull(bytes);

        bytes = hessianIO(output -> Try.run(() -> output.writeBytes(null, 0, 0)), i -> Try.of(i::readBytes)
                .get());
        assertNull(bytes);
    }

    @Test
    public void testByte() throws IOException {
        // Test byte
        byte value = 1;

        byte ret = (byte) hessianIO(output -> Try.run(() -> output.writeObject(value)), i -> Try.of(i::readObject)
                .get());

        assertEquals(value, ret);
    }

    @Test
    public void testInvalidType() {
        byte[] invalidData = {'X'};
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(invalidData));

        // Should throw IOException
        assertThrows(IOException.class, input::readBoolean);
    }

    @Test
    public void testReadObject() throws IOException {
        // Test reading a custom object
        TestObject testObject = new TestObject("John", 30);

        TestObject result = (TestObject)
                hessianIO(output -> Try.run(() -> output.writeObject(testObject)), input -> Try.of(input::readObject)
                        .get());

        assertEquals(testObject.getName(), result.getName());
        assertEquals(testObject.getAge(), result.getAge());

        result = (TestObject) hessianIO(
                output -> Try.run(() -> output.writeObject(testObject)),
                input -> Try.of(() -> input.readObject(TestObject.class)).get());

        assertEquals(testObject.getName(), result.getName());
        assertEquals(testObject.getAge(), result.getAge());

        Map map = (Map) hessianIO(
                output -> Try.run(() -> output.writeObject(testObject)),
                input -> Try.of(() -> input.readObject(Map.class)).get());

        assertEquals(testObject.getName(), map.get("name"));
        assertEquals(testObject.getAge(), map.get("age"));

        // Test reading an un-serializable object
        UnSerializableObject unSerializableObject = new UnSerializableObject();
        assertThrows(
                IOException.class,
                () -> hessianIO(
                        output -> Try.run(() -> output.writeObject(unSerializableObject)),
                        input -> Try.of(input::readObject).get()));

        Object ret = hessianIO(
                output -> Try.run(() -> output.writeInt(Integer.MAX_VALUE)),
                input -> Try.of(input::readObject).get());
        assertEquals(Integer.MAX_VALUE, ret);

        List<TestObject> list = new ArrayList<>();
        list.add(new TestObject("a", 1));
        list.add(new TestObject("b", 2));
        Object o = hessianIO(output -> Try.run(() -> output.writeObject(list)), input -> Try.of(input::readObject)
                .get());
        assertEquals(list.size(), ((List) o).size());
    }

    @Test
    public void testEnum() throws Exception {
        Object obj = hessianIO(output -> Try.run(() -> output.writeObject(Enum.A)), input -> Try.of(input::readObject)
                .get());
        assertEquals(Enum.A, obj);
    }

    @Test
    public void testEnumeration() throws Exception {
        Vector<String> vector = new Vector<>();
        vector.add("a");
        vector.add("b");
        List<String> obj = (List) hessianIO(
                output -> Try.run(() -> output.writeObject(vector.elements())),
                input -> Try.of(input::readObject).get());
        assertEquals(vector.get(0), obj.get(0));
        assertEquals(vector.get(1), obj.get(1));
    }

    @Test
    public void testArray() throws Exception {
        TestObject[] array = new TestObject[] {new TestObject("A", 1), new TestObject("B", 2)};
        TestObject[] obj = (TestObject[])
                hessianIO(output -> Try.run(() -> output.writeObject(array)), input -> Try.of(input::readObject)
                        .get());
        assertEquals(array[0].getName(), obj[0].getName());

        TestObject[] array10 =
                IntStream.range(0, 10).mapToObj(i -> new TestObject("A" + i, i)).toArray(TestObject[]::new);
        obj = (TestObject[])
                hessianIO(output -> Try.run(() -> output.writeObject(array10)), input -> Try.of(input::readObject)
                        .get());
        assertEquals(array10[0].getName(), obj[0].getName());
    }

    @Test
    public void testVariableLengthList() throws Exception {
        List<Integer> list = Arrays.asList(10, 20);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);
        out.writeListBegin(-1, "java.util.ArrayList");
        for (Object v : list) {
            out.writeObject(v);
        }
        out.writeListEnd();
        out.flush();

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));
        Object obj = input.readObject();
        assertEquals(list, obj);
    }

    @Test
    public void testAnnotation() throws IOException {
        // Test reading an annotation
        Annotation[] annotations = AnnotationTest.class.getAnnotations();

        Annotation annotation = (Annotation) hessianIO(
                output -> Try.run(() -> output.writeObject(annotations[0])),
                input -> Try.of(input::readObject).get());

        assertEquals("test123", ((TestAnnotation) annotation).value());
    }

    @Test
    public void testBlackWhiteList() throws IOException {
        Hessian2SerializerFactory factory = new Hessian2SerializerFactory();

        factory.getClassFactory().allow("io.github.wuwen5.hessian.io.HessianIOTest*");
        factory.getClassFactory().deny("io.github.wuwen5.hessian.io.beans.black.*");

        Object o = hessianIOBeanSerializeFactory(
                output -> Try.run(() -> output.writeObject(new DenyObj().setName("abc"))),
                input -> Try.of(input::readObject).get(),
                factory);
        Object o2 = hessianIOBeanSerializeFactory(
                output -> Try.run(() -> output.writeObject(new TestObject("a", 1))),
                input -> Try.of(input::readObject).get(),
                factory);
        Object o3 = hessianIOBeanSerializeFactory(
                output -> Try.run(() -> output.writeObject(new BaseUser())),
                input -> Try.of(input::readObject).get(),
                factory);

        Assertions.assertInstanceOf(Map.class, o);
        Assertions.assertInstanceOf(TestObject.class, o2);
        Assertions.assertInstanceOf(Map.class, o3);
    }

    @Test
    public void testBlackWhiteList2() throws IOException {
        Object o = hessianIO(
                output -> Try.run(() -> output.writeObject(new DenyObj().setName("abc"))), input -> Try.of(() -> {
                            input.allow("io.github.wuwen5.hessian.io.HessianIOTest*");
                            return input.readObject();
                        })
                        .get());

        Object o2 =
                hessianIO(output -> Try.run(() -> output.writeObject(new TestObject("a", 1))), input -> Try.of(() -> {
                            input.allow("io.github.wuwen5.hessian.io.HessianIOTest*");
                            return input.readObject();
                        })
                        .get());
        Object o3 = hessianIO(output -> Try.run(() -> output.writeObject(new BaseUser())), input -> Try.of(() -> {
                    input.allow("io.github.wuwen5.hessian.io.HessianIOTest*");
                    return input.readObject();
                })
                .get());

        Assertions.assertInstanceOf(Map.class, o);
        Assertions.assertInstanceOf(TestObject.class, o2);
        Assertions.assertInstanceOf(Map.class, o3);
    }

    @Test
    void testReadInputStream() throws Exception {

        HessianDecoder.setCloseStreamOnClose(true);
        assertTrue(HessianDecoder.isCloseStreamOnClose());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HessianEncoder out = new HessianEncoder(bos);

        byte[] data = "hello".getBytes();
        out.writeBytes(data);
        out.flush();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        HessianDecoder in = new HessianDecoder(bis);

        try (InputStream inputStream = in.readInputStream()) {
            assertNotNull(inputStream);

            byte[] buffer = inputStream.readAllBytes();
            assertArrayEquals(data, buffer);
        }

        bis = new ByteArrayInputStream(bos.toByteArray());
        in = new HessianDecoder(bis);

        try (InputStream inputStream = in.readInputStream()) {
            assertNotNull(inputStream);

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            int b;
            while ((b = inputStream.read()) != -1) {
                result.write(b);
            }

            assertArrayEquals(data, result.toByteArray());
        }

        // test null
        bos.reset();
        out.writeNull();
        out.close();
        bis = new ByteArrayInputStream(bos.toByteArray());
        in = new HessianDecoder(bis);
        assertNull(in.readInputStream());
    }

    @Test
    public void testInitPacket() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output();
        out.initPacket(baos);
        out.writeObject("hello hessian");
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);
        in.initPacket(bais);

        Object value = in.readObject();
        assertEquals("hello hessian", value);
        in.close();
    }

    /**
     * Demonstrate the chunk scenario: write a long string, Hessian2 will transmit it in chunks
     */
    @Test
    public void testChunkedStringWithInitPacket() throws Exception {
        String longString = "x1".repeat(20_000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);
        out.writeString(longString);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);
        in.initPacket(bais);

        Object value = in.readObject();
        assertEquals(longString, value);

        in.close();
    }

    @Test
    public void testChunkedString() throws Exception {
        String longString = "x1".repeat(20_000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);
        OutputStream bytesOutputStream = out.getBytesOutputStream();
        bytesOutputStream.write(longString.getBytes(), 0, longString.length() - 100);

        for (int i = longString.length() - 100; i < longString.length(); i++) {
            bytesOutputStream.write(longString.charAt(i));
        }

        bytesOutputStream.close();
        out.close();

        byte[] byteArray = baos.toByteArray();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                Hessian2Input in = new Hessian2Input(bais)) {
            in.initPacket(bais);

            byte[] bytes = new byte[longString.length()];
            in.readBytes(bytes, 0, longString.length());
            assertEquals(longString, new String(bytes, StandardCharsets.UTF_8));
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                Hessian2Input in = new Hessian2Input(bais)) {
            in.initPacket(bais);

            int idx = 0;
            int i;

            byte[] bytes = new byte[longString.length()];

            while ((i = in.readByte()) != -1) {
                bytes[idx++] = (byte) i;
            }

            String result = new String(bytes, 0, idx, StandardCharsets.UTF_8);
            assertEquals(longString, result);
        }
    }

    @Test
    public void testReadStringWithBuffer() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);

        String inputStr = "x1".repeat(20_000);
        out.writeString(inputStr);
        out.flush();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                Hessian2Input in = new Hessian2Input(bais)) {

            char[] buf = new char[8];
            StringBuilder sb = new StringBuilder();

            int len;
            while ((len = in.readString(buf, 0, buf.length)) > 0) {
                sb.append(buf, 0, len);
            }

            assertEquals(inputStr, sb.toString());
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                Hessian2Input in = new Hessian2Input(bais)) {

            int i;
            StringBuilder sb = new StringBuilder();
            while ((i = in.readChar()) != -1) {
                sb.append((char) i);
            }

            assertEquals(inputStr, sb.toString());
        }

        Integer i = hessianIO(output -> Try.run(output::writeNull), input -> Try.of(input::readChar)
                .get());

        assertEquals(-1, i);
        i = hessianIO(output -> Try.run(output::writeNull), input -> Try.of(input::readByte)
                .get());

        assertEquals(-1, i);

        String s = hessianIO(output -> Try.run(output::writeNull), input -> Try.of(() -> {
                    char[] buf = new char[8];
                    StringBuilder sb = new StringBuilder();
                    int len;
                    while ((len = input.readString(buf, 0, buf.length)) > 0) {
                        sb.append(buf, 0, len);
                    }
                    return sb.toString();
                })
                .get());
        assertEquals("", s);
    }

    @Test
    void testShortString() throws IOException {
        String s = "short";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);
        out.writeString(s);
        out.flush();

        Hessian2Input in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));
        char[] buf = new char[10];
        int len = in.readString(buf, 0, buf.length);
        assertEquals(s.length(), len);
        assertEquals(s, new String(buf, 0, len));

        // short string write char array
        baos.reset();
        out.writeString(s.toCharArray(), 0, s.length());
        out.flush();

        in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));
        buf = new char[10];
        len = in.readString(buf, 0, buf.length);
        assertEquals(s.length(), len);
        assertEquals(s, new String(buf, 0, len));

        // test bytes
        baos.reset();
        out.writeBytes(s.getBytes());
        out.flush();
        in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

        int i;
        int idx = 0;

        byte[] bytes = new byte[s.length()];

        while ((i = in.readByte()) != -1) {
            bytes[idx++] = (byte) i;
        }

        assertEquals(s.length(), idx);
        String result = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(s, result);
    }

    @Test
    void testMediumString() throws IOException {
        // 长度 40 -> 触发 0x30-0x33 分支
        String s = "a".repeat(40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);
        out.writeString(s);
        out.flush();

        Hessian2Input in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));
        char[] buf = new char[100];
        int len = in.readString(buf, 0, buf.length);
        assertEquals(s.length(), len);
        assertTrue(new String(buf).startsWith("aa"));

        // char array
        baos.reset();
        out.writeString(s.toCharArray(), 0, s.length());
        out.flush();

        in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));
        buf = new char[100];
        len = in.readString(buf, 0, buf.length);
        assertEquals(s.length(), len);
        assertTrue(new String(buf).startsWith("aa"));

        // test readObject
        baos.reset();
        out.writeString(s);
        out.flush();
        in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

        Object o = in.readObject();
        assertEquals(s, o);

        // test write char array
        baos.reset();
        String str = "a".repeat(2048);
        out.writeString(str.toCharArray(), 0, str.length());
        out.flush();
        in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

        o = in.readObject();
        assertEquals(str, o);
    }

    @Test
    void readString_entersElse_and_readsNextTag_toLastChunk() throws Exception {

        disableLog(HessianDecoder.class);
        // 长度 > 0x8000，确保会分块：第一块是 BC_STRING_CHUNK，最后一块是 BC_STRING
        String s = "x".repeat(0x8000 + 10);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);
        out.writeString(s);
        out.flush();

        Hessian2Input in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

        // 缓冲区要“足够大”，一次 readString 跨越多个 chunk
        char[] buf = new char[s.length()];
        int n = in.readString(buf, 0, buf.length);

        assertEquals(s.length(), n);
        assertEquals(s, new String(buf, 0, n));
        in.close();

        // write char array
        baos.reset();
        out.writeString(s.toCharArray(), 0, s.length());
        out.flush();

        in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

        // 缓冲区要“足够大”，一次 readString 跨越多个 chunk
        buf = new char[100];
        in.readString(buf, 0, buf.length);

        assertTrue(new String(buf).startsWith("xx"));
        in.close();

        // test bytes
        baos.reset();
        out.writeBytes(s.getBytes());
        out.flush();
        in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

        int i;
        int idx = 0;

        byte[] bytes = new byte[s.length()];

        while ((i = in.readByte()) != -1) {
            bytes[idx++] = (byte) i;
        }

        assertEquals(s.length(), idx);
        String result = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(s, result);
        enableLog(HessianDecoder.class);
    }

    @Test
    void readString_entersElse_multipleTimes_readsChunk_thenLast() throws Exception {

        disableLog(HessianDecoder.class);
        try {
            // 三段：0x8000 + 0x8000 + 5
            String s = "y".repeat(0x8000 * 2 + 5);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Hessian2Output out = new Hessian2Output(baos);
            out.writeString(s);
            out.flush();

            Hessian2Input in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

            char[] buf = new char[s.length()];
            int n = in.readString(buf, 0, buf.length);

            assertEquals(s.length(), n);
            assertEquals(s, new String(buf, 0, n));

            baos.reset();
            out.writeString(s.toCharArray(), 0, s.length());
            out.flush();

            in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

            buf = new char[s.length()];
            n = in.readString(buf, 0, buf.length);

            assertEquals(s.length(), n);
            assertEquals(s, new String(buf, 0, n));

            baos.reset();
            byte[] bytes = s.getBytes();
            out.writeBytes(bytes, 0, bytes.length);
            out.flush();

            in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

            byte[] bytes1 = in.readBytes();

            assertEquals(bytes.length, bytes1.length);
            assertEquals(s, new String(bytes1, StandardCharsets.UTF_8));

            // test bytes
            baos.reset();
            out.writeBytes(s.getBytes());
            out.flush();
            in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

            int i;
            int idx = 0;

            bytes = new byte[s.length()];

            while ((i = in.readByte()) != -1) {
                bytes[idx++] = (byte) i;
            }

            assertEquals(s.length(), idx);
            String result = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(s, result);

            // test readObject
            baos.reset();
            out.writeBytes(s.getBytes());
            out.flush();
            in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));

            assertEquals(s, new String((byte[]) in.readObject(), StandardCharsets.UTF_8));

            // test null
            baos.reset();
            out.writeString(null, 0, 0);
            out.flush();

            in = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()));
            assertNull(in.readObject());
        } finally {
            enableLog(HessianDecoder.class);
        }
    }

    @Test
    void testInvalidTag() {
        // 手工构造一个非法 tag 0x7f
        byte[] invalid = {(byte) 0x7f};
        Hessian2Input in = new Hessian2Input(new ByteArrayInputStream(invalid));
        char[] buf = new char[10];
        assertThrows(IOException.class, () -> in.readString(buf, 0, buf.length));
    }

    @Test
    void testWriteVersion() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(outputStream);
        output.writeVersion();
        output.close();

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        int read = input.read();
        int read1 = input.read();
        int read2 = input.read();

        assertEquals('H', read);
        assertEquals(2, read1);
        assertEquals(0, read2);
    }

    @Test
    void testPrintLenStringNormal() throws IOException {
        String value = "Hello";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(baos);
        output.printLenString(value);
        output.flush();

        try (Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()))) {

            input.read();
            byte[] result = baos.toByteArray();
            // 长度写在前两个字节
            int len = ((result[0] & 0xFF) << 8) | (result[1] & 0xFF);
            assertEquals(value.length(), len);

            assertEquals(value, input.readString());
        }

        baos.reset();
        output.printLenString(null);
        try (Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(baos.toByteArray()))) {
            assertEquals(-1, input.read());
            assertThrows(HessianProtocolException.class, input::readString);
        }
    }

    @Test
    void testPrintStringNormal() throws IOException {
        String value = "Hello";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(baos);
        output.printString(value);
        output.close();
        assertEquals(value, baos.toString());
    }

    enum Enum {
        A,
        B,
        C
    }

    @TestAnnotation("test123")
    static class AnnotationTest implements Serializable {}

    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        String value();
    }

    @Getter
    @Setter
    static class TestObject implements Serializable {
        private String name;
        private int age;

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class UnSerializableObject {}
}
