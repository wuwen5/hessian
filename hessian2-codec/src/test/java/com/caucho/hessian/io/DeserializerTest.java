package com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.AbstractHessianDecoder;
import io.github.wuwen5.hessian.io.Deserializer;
import io.github.wuwen5.hessian.io.HessianProtocolException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class DeserializerTest {

    @Test
    void testDemoDeserializer() throws IOException {
        DemoDeserializer demoDeserializer = new DemoDeserializer();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Hessian2Output output = new Hessian2Output(outputStream)) {
            output.writeObject(new Demo());
        }

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        input.setSerializerFactory(new SerializerFactory() {
            @Override
            public Deserializer getDeserializer(Class<?> cl) throws HessianProtocolException {
                if (cl == Demo.class) {
                    return demoDeserializer;
                }
                return super.getDeserializer(cl);
            }
        });

        Object o = input.readObject();
        Assertions.assertEquals(Demo.class, o.getClass());
    }

    static class Demo implements Serializable {}

    static class DemoDeserializer extends AbstractDeserializer {

        @Override
        public Object readObject(AbstractHessianDecoder in, Object[] fields) {
            return new Demo();
        }
    }
}
