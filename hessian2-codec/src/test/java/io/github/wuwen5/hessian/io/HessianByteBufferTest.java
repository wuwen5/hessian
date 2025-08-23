package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HessianByteBufferTest {

    @Test
    void testWriteSmallByteBuffer() throws Exception {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);

        out.writeByteBufferStart();
        out.writeByteBufferEnd(data, 0, data.length);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);

        byte[] read = in.readBytes();
        assertArrayEquals(data, read);
    }

    @Test
    void testWriteLargeByteBufferInParts() throws Exception {
        // 6KB
        byte[] bigData = new byte[6 * 1024];
        for (int i = 0; i < bigData.length; i++) {
            bigData[i] = (byte) (i % 256);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);

        out.writeByteBufferStart();

        int chunkSize = 1024;
        int written = 0;
        while (written + chunkSize < bigData.length) {
            out.writeByteBufferPart(bigData, written, chunkSize);
            written += chunkSize;
        }

        int remain = bigData.length - written;
        out.writeByteBufferEnd(bigData, written, remain);

        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);
        byte[] read = in.readBytes();

        assertArrayEquals(bigData, read);
    }
}
