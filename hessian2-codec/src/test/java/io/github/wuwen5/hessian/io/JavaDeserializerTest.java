package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class JavaDeserializerTest {
    @Test
    void testReadMapWithObject() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Hessian2Output h2o = new Hessian2Output(out);
        JavaBeanWithReplace bean =
                new JavaBeanWithReplace("Alice", 20, true, (byte) 1, (short) 2, 'c', 100L, 1.0f, 2.0d);
        h2o.writeObject(bean);
        h2o.flush();
        h2o.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Hessian2Input h2i = new Hessian2Input(in);
        h2i.readMapStart();
        JavaDeserializer deserializer =
                new JavaDeserializer(JavaBeanWithReplace.class, new FieldDeserializer2Factory());
        Object result = deserializer.readMap(h2i);
        assertInstanceOf(JavaBeanWithReplace.class, result);
        assertEquals("Alice", ((JavaBeanWithReplace) result).getName());

        JavaDeserializer deserializer2 = new JavaDeserializer(Serializable.class, new FieldDeserializer2Factory());
        assertThrows(HessianProtocolException.class, () -> deserializer2.readMap(h2i));
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class JavaBeanWithReplace implements Serializable {

        private String name;
        private int age;
        private boolean bool;
        private byte aByte;
        private short aShort;
        private char aChar;
        private long aLong;
        private float aFloat;
        private double aDouble;

        Object writeReplace() {
            Map<String, String> map = new HashMap<>();
            map.put("name", name);
            return map;
        }
    }
}
