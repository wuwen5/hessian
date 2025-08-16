package io.github.wuwen5.hessian.io;

import io.vavr.control.Try;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class BeanSerializerFactoryTest extends SerializeTestBase {

    @Test
    void testBeanSerializerFactoryWithReplace() throws IOException {
        SerializerFactory serializerFactory = new BeanSerializerFactory();
        JavaBeanWithReplace test123 = new JavaBeanWithReplace().setName("test123");

        Object ret = hessianIOBeanSerializeFactory(
                out -> Try.run(() -> out.writeObject(test123)),
                in -> Try.of(() -> in.readObject(JavaBeanWithReplace.class)).get(),
                serializerFactory);
        Assertions.assertEquals(test123, ret);
    }

    @Test
    void testBeanSerializerFactory() throws IOException {
        SerializerFactory serializerFactory = new BeanSerializerFactory();
        JavaBean test123 = new JavaBean().setName("test123");

        Object ret = hessianIOBeanSerializeFactory(
                out -> Try.run(() -> out.writeObject(test123)),
                in -> Try.of(in::readObject).get(),
                serializerFactory);
        Assertions.assertEquals(test123, ret);
    }

    @Test
    void testBeanSerializerWithConstructor() throws IOException {
        SerializerFactory serializerFactory = new BeanSerializerFactory();
        JavaBean2 test123 = new JavaBean2("test123", true, (byte) 1, (short) 2, 3, 4L, 5.0f, 6.0);

        Object ret = hessianIOBeanSerializeFactory(
                out -> Try.run(() -> out.writeObject(test123)),
                in -> Try.of(in::readObject).get(),
                serializerFactory);
        Assertions.assertEquals(test123, ret);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static class JavaBeanWithReplace {

        private String name;

        Object writeReplace() {
            Map<String, String> map = new HashMap<>();
            map.put("name", name);
            return map;
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static class JavaBean {

        private String name;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class JavaBean2 {
        private String name;
        private boolean bool;
        private byte byteField;
        private short shortField;
        private int intField;
        private long longField;
        private float floatField;
        private double doubleField;
    }
}
