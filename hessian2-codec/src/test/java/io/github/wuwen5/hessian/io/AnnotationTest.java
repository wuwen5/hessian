package io.github.wuwen5.hessian.io;

import io.vavr.control.Try;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class AnnotationTest extends SerializeTestBase {

    @Test
    void test() throws IOException {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);

        TestAnnotation testAnnotation = baseHessian2Serialize(annotation);

        assertCheck(testAnnotation, annotation);
    }

    @Test
    void testMap() throws IOException {
        TestAnnotation annotation = AnnotatedClass.class.getAnnotation(TestAnnotation.class);

        Map<String, Object> map = new HashMap<>();
        map.put("value", annotation.value());
        map.put("byteValue", annotation.byteValue());
        map.put("boolValue", annotation.boolValue());
        map.put("longValue", annotation.longValue());
        map.put("floatValue", annotation.floatValue());
        map.put("doubleValue", annotation.doubleValue());
        map.put("shortValue", annotation.shortValue());

        TestAnnotation testAnnotation = (TestAnnotation) hessianIO(
                out -> Try.run(() -> out.writeObject(map)),
                in -> Try.of(() -> in.readObject(TestAnnotation.class)).get());

        assertCheck(testAnnotation, annotation);
    }

    private static void assertCheck(TestAnnotation testAnnotation, TestAnnotation annotation) {
        Assertions.assertEquals(TestAnnotation.class, testAnnotation.annotationType());
        Assertions.assertEquals(annotation, testAnnotation);
        // validate invocation handler equals
        Assertions.assertEquals(testAnnotation, annotation);
        Assertions.assertDoesNotThrow(testAnnotation::toString);
        Assertions.assertDoesNotThrow(testAnnotation::hashCode);
    }

    @TestAnnotation(byteValue = 1, boolValue = true)
    public static class AnnotatedClass {}

    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface TestAnnotation {
        String value() default "default";

        byte byteValue() default 0;

        short shortValue() default 0;

        boolean boolValue() default false;

        long longValue() default 1L;

        float floatValue() default 1.0001f;

        double doubleValue() default 2.0001;
    }
}
