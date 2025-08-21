package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class HessianDebugStreamTest {
    @Test
    void testDebugOutputStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringWriter debugWriter = new StringWriter();

        try (HessianDebugOutputStream debugOut = new HessianDebugOutputStream(bos, new PrintWriter(debugWriter))) {
            debugOut.startTop2();
            HessianEncoder encoder = new HessianEncoder(debugOut);
            encoder.writeString("ABC");
            encoder.flush();
        }

        String debugLog = debugWriter.toString();
        assertTrue(debugLog.contains("ABC"));

        debugWriter = new StringWriter();
        try (HessianDebugInputStream debugIn = new HessianDebugInputStream(
                new ByteArrayInputStream(bos.toByteArray()), new PrintWriter(debugWriter))) {
            debugIn.startTop2();
            HessianDecoder decoder = new HessianDecoder(debugIn);
            String s = decoder.readString();
            assertEquals("ABC", s);
            decoder.close();
        }

        assertTrue(debugLog.contains("ABC"));
    }

    @Test
    void testDebugOutputStream2() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringWriter debugWriter = new StringWriter();

        try (HessianDebugOutputStream debugOut = new HessianDebugOutputStream(debugWriter::append)) {
            debugOut.initPacket(bos);
            debugOut.startTop2();
            HessianEncoder encoder = new HessianEncoder(debugOut);
            encoder.writeString("ABC");
            encoder.flush();
        }
        String debugLog = debugWriter.toString();
        assertTrue(debugLog.contains("ABC"));

        debugWriter = new StringWriter();
        try (HessianDebugInputStream debugIn = new HessianDebugInputStream(debugWriter::append)) {
            debugIn.initPacket(new ByteArrayInputStream(bos.toByteArray()));
            debugIn.startTop2();
            HessianDecoder decoder = new HessianDecoder(debugIn);
            String s = decoder.readString();
            assertEquals("ABC", s);
            decoder.close();
        }

        assertTrue(debugLog.contains("ABC"));
    }
}
