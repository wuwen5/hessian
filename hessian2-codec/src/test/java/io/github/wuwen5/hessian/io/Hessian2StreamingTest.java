package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wuwen5.hessian.io.beans.BasicTypeBean;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class Hessian2StreamingTest {
    @Test
    void testWriteAndReadStreamingData() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2StreamingOutput out = new Hessian2StreamingOutput(bos);

        BasicTypeBean basicTypeBean = BasicTypeBean.create();
        out.writeObject(basicTypeBean);
        out.flush();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Hessian2StreamingInput in = new Hessian2StreamingInput(bis);

        BasicTypeBean obj = (BasicTypeBean) in.readObject();

        assertEquals(basicTypeBean, obj);
    }

    @Test
    void testWriteAndReadNull() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2StreamingOutput out = new Hessian2StreamingOutput(bos);
        out.setCloseStreamOnClose(true);
        out.writeObject(null);
        out.flush();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Hessian2StreamingInput in = new Hessian2StreamingInput(bis);

        Object result = in.readObject();
        assertTrue(out.isCloseStreamOnClose());
        assertNull(result);
    }
}
