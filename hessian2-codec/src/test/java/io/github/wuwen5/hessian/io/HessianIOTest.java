package io.github.wuwen5.hessian.io;

import static io.github.wuwen5.hessian.io.Hessian2Constants.INT_BYTE_MIN;
import static io.github.wuwen5.hessian.io.Hessian2Constants.INT_DIRECT_MIN;
import static io.github.wuwen5.hessian.io.Hessian2Constants.INT_SHORT_MIN;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Disabled;
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
        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(false)), input -> Try.of(input::readInt)
                .get());
        assertEquals(0, ret);
        ret = hessianIO(output -> Try.run(() -> output.writeBoolean(true)), input -> Try.of(input::readInt)
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
        java.sql.Date sqlDate = (java.sql.Date) hessianIO(
                output -> Try.run(() -> output.writeObject(new java.sql.Date(now))),
                input -> Try.of(input::readObject).get());
        assertEquals(now, l);
        assertEquals(now, o.getTime());
        assertEquals(now, sqlDate.getTime());
    }

    /**
     * TODO
     */
    @Test
    @Disabled
    public void testLocalDate() throws IOException {
        // Test local date
        LocalDate now = LocalDate.now();

        LocalDate localDate = (LocalDate)
                hessianIO(output -> Try.run(() -> output.writeObject(now)), input -> Try.of(input::readObject)
                        .get());
        assertEquals(now, localDate);
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
    }

    @Test
    public void testInvalidType() {
        byte[] invalidData = {'X'};
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(invalidData));

        // Should throw IOException
        assertThrows(IOException.class, () -> input.readBoolean());
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
