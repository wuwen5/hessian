package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.caucho.hessian.io.AbstractDeserializer;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ContextSerializerFactoryTest {

    static class MyPojo {
        String name;
    }

    static class MyPojoHessianSerializer extends AbstractSerializer {
        @Override
        public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
            MyPojo pojo = (MyPojo) obj;
            out.writeString(pojo.name);
        }
    }

    static class MyPojoHessianDeserializer extends AbstractDeserializer {
        @Override
        public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
            MyPojo pojo = new MyPojo();
            pojo.name = in.readString();
            return pojo;
        }
    }

    @Test
    void testGetCustomSerializer() {
        ContextSerializerFactory contextFactory = ContextSerializerFactory.create();
        Serializer serializer = contextFactory.getCustomSerializer(MyPojo.class);
        Deserializer deserializer = contextFactory.getCustomDeserializer(MyPojo.class);
        assertNotNull(serializer);
        assertNotNull(deserializer);
        assertInstanceOf(MyPojoHessianSerializer.class, serializer);
        assertInstanceOf(MyPojoHessianDeserializer.class, deserializer);
    }
}
