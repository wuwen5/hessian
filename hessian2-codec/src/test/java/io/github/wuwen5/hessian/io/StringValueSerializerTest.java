package io.github.wuwen5.hessian.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class StringValueSerializerTest {
    @Test
    void testStringValueSerializer() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (HessianEncoder hessianEncoder = new HessianEncoder(out)) {
            StringValueSerializer.SER.writeObject("abc", hessianEncoder);
        }

        HessianDecoder hessianDecoder = new HessianDecoder(new ByteArrayInputStream(out.toByteArray()));
        StringValueDeserializer deserializer = new StringValueDeserializer(String.class);
        // read the object type
        hessianDecoder.read();
        Object o = deserializer.readObject(hessianDecoder, new String[] {"value"});
        assert o instanceof String;
    }
}
