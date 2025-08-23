package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

public class HessianIOLoggerTest extends SerializeTestBase {

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
    }

    @Test
    public void testInitPacket() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);
        out.writeObject("hello hessian");
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);
        in.initPacket(bais);

        Object value = in.readObject();
        assertEquals("hello hessian", value);
        in.close();
    }

    @Test
    void testInvalidTag() {
        // 手工构造一个非法 tag 0x7f
        byte[] invalid = {(byte) 0x7f};
        Hessian2Input in = new Hessian2Input(new ByteArrayInputStream(invalid));
        char[] buf = new char[10];
        assertThrows(IOException.class, () -> in.readString(buf, 0, buf.length));
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
}
