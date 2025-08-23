package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HessianWriteByteStreamTest {

    /**
     * 一个简单的扩展类，用来暴露 writeByteStream 方法，
     * 并允许我们抓取 flushBuffer 后的输出。
     */
    static class TestHessianEncoder extends Hessian2Output {
        public TestHessianEncoder(OutputStream os) {
            super(os);
        }

        // 这里直接调用父类的 writeByteStream
        public void callWriteByteStream(InputStream is) throws IOException {
            writeByteStream(is);
            flushBuffer();
        }
    }

    @Test
    void testWriteByteStreamWithSmallData() throws Exception {
        byte[] data = "hello hessian".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TestHessianEncoder encoder = new TestHessianEncoder(baos);

        encoder.callWriteByteStream(bis);
        encoder.close();

        // 读回
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);
        byte[] read = in.readBytes();

        assertArrayEquals(data, read);
    }

    @Test
    void testWriteByteStreamWithLargeDataTriggerFlush() throws Exception {
        // 准备比内部缓冲区略大的数据，比如 20KB
        byte[] data = new byte[20 * 1024];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TestHessianEncoder encoder = new TestHessianEncoder(baos);

        encoder.callWriteByteStream(bis);
        encoder.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);
        byte[] read = in.readBytes();

        assertArrayEquals(data, read);
    }

    @Test
    void testWriteByteStreamWithEmptyInput() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TestHessianEncoder encoder = new TestHessianEncoder(baos);

        encoder.callWriteByteStream(bis);
        encoder.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);
        byte[] read = in.readBytes();

        // 空流应返回空数组
        assertArrayEquals(new byte[0], read);
    }
}
