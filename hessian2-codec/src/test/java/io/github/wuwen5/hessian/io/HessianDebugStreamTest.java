package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
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
            HessianEncoder encoder = new HessianEncoder(debugOut) {
                public void writeString(String string) throws IOException {
                    super.writeString(string);

                    writeVersion();

                    flushIfFull();

                    buffer[offset++] = (byte) 'R';
                    super.writeString("PONG");
                }
            };
            encoder.writeString("PING");
            encoder.flush();
        }

        String debugLog = debugWriter.toString();
        assertTrue(debugLog.contains("PING"));
        assertTrue(debugLog.contains("PONG"));

        debugWriter = new StringWriter();
        try (HessianDebugInputStream debugIn = new HessianDebugInputStream(
                new ByteArrayInputStream(bos.toByteArray()), new PrintWriter(debugWriter))) {
            debugIn.startTop2();
            debugIn.startStreaming();
            HessianDecoder decoder = new HessianDecoder(debugIn);
            String s = decoder.readString();
            assertEquals("PING", s);
            decoder.close();
        }

        assertTrue(debugLog.contains("PONG"));
    }

    @Test
    void testDebugOutputStream2() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringWriter debugWriter = new StringWriter();

        List<Obj> list = new ArrayList<>();
        Obj obj = new Obj();
        obj.setValue("ABC");
        list.add(obj);
        list.add(obj);

        try (HessianDebugOutputStream debugOut = new HessianDebugOutputStream(debugWriter::append)) {
            debugOut.initPacket(bos);
            debugOut.startTop2();
            HessianEncoder encoder = new HessianEncoder(debugOut);
            encoder.writeObject(list);
            encoder.flush();
        }

        debugWriter = new StringWriter();
        try (HessianDebugInputStream debugIn = new HessianDebugInputStream(debugWriter::append)) {
            debugIn.initPacket(new ByteArrayInputStream(bos.toByteArray()));
            debugIn.startTop2();
            HessianDecoder decoder = new HessianDecoder(debugIn);
            List<Obj> s = (List) decoder.readObject();
            assertEquals("ABC", s.get(0).getValue());
            assertSame(s.get(0), s.get(1));
            decoder.close();
        }
    }

    @Test
    void testDebugObjectDirectMax() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringWriter debugWriter = new StringWriter();

        List<Object> list = new ArrayList<>();
        Obj obj = new Obj();
        obj.setValue("ABC");
        list.add(obj);
        list.add(new Obj1());
        list.add(new Obj2());
        list.add(new Obj3());
        list.add(new Obj4());
        list.add(new Obj5());
        list.add(new Obj6());
        list.add(new Obj7());
        list.add(new Obj8());
        list.add(new Obj9());
        list.add(new Obj10());
        list.add(new Obj11());
        list.add(new Obj12());
        list.add(new Obj13());
        list.add(new Obj14());
        list.add(new Obj15());
        list.add(new Obj16());

        try (HessianDebugOutputStream debugOut = new HessianDebugOutputStream(debugWriter::append)) {
            debugOut.initPacket(bos);
            debugOut.startTop2();
            HessianEncoder encoder = new HessianEncoder(debugOut);
            encoder.writeObject(list);
            encoder.flush();
        }

        debugWriter = new StringWriter();
        try (HessianDebugInputStream debugIn = new HessianDebugInputStream(debugWriter::append);
                HessianDecoder decoder = new HessianDecoder(debugIn)) {
            debugIn.initPacket(new ByteArrayInputStream(bos.toByteArray()));
            debugIn.startTop2();
            List<Object> s = (List) decoder.readObject();
            assertEquals("ABC", ((Obj) s.get(0)).getValue());
            assertEquals(Obj16.class, s.get(s.size() - 1).getClass());
        }
    }

    @Test
    public void testChunkedString() throws Exception {
        String longString = "x1".repeat(50_000);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringWriter debugWriter = new StringWriter();

        try (HessianDebugOutputStream debugOut = new HessianDebugOutputStream(bos, new PrintWriter(debugWriter))) {
            debugOut.initPacket(bos);
            debugOut.startTop2();

            HessianEncoder encoder = new HessianEncoder(debugOut);

            OutputStream bytesOutputStream = encoder.getBytesOutputStream();
            bytesOutputStream.write(longString.getBytes(), 0, longString.length() - 100);

            for (int i = longString.length() - 100; i < longString.length(); i++) {
                bytesOutputStream.write(longString.charAt(i));
            }

            bytesOutputStream.close();
            encoder.close();
        }

        try (HessianDebugInputStream debugIn = new HessianDebugInputStream(debugWriter::append)) {
            debugIn.initPacket(new ByteArrayInputStream(bos.toByteArray()));
            debugIn.startTop2();

            HessianDecoder decoder = new HessianDecoder(debugIn);

            byte[] bytes = new byte[longString.length()];
            decoder.readBytes(bytes, 0, longString.length());
            assertEquals(longString, new String(bytes, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testWriteStreamingObject() throws Exception {
        String str = "x1".repeat(100);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringWriter debugWriter = new StringWriter();

        try (HessianDebugOutputStream debugOut = new HessianDebugOutputStream(bos, new PrintWriter(debugWriter))) {
            debugOut.initPacket(bos);
            debugOut.startTop2();

            HessianEncoder encoder = new HessianEncoder(debugOut);

            encoder.writeStreamingObject(str);

            encoder.close();
        }

        try (HessianDebugInputStream debugIn = new HessianDebugInputStream(debugWriter::append)) {
            debugIn.initPacket(new ByteArrayInputStream(bos.toByteArray()));
            debugIn.startTop2();

            Hessian2StreamingInput input = new Hessian2StreamingInput(debugIn);

            Object o = input.readObject();
            assertEquals(str, o);
        }
    }

    @Getter
    @Setter
    static class Obj implements Serializable {
        private String value;
    }

    @Getter
    @Setter
    static class Obj1 implements Serializable {}

    @Getter
    @Setter
    static class Obj2 implements Serializable {}

    @Getter
    @Setter
    static class Obj3 implements Serializable {}

    @Getter
    @Setter
    static class Obj4 implements Serializable {}

    @Getter
    @Setter
    static class Obj5 implements Serializable {}

    @Getter
    @Setter
    static class Obj6 implements Serializable {}

    @Getter
    @Setter
    static class Obj7 implements Serializable {}

    @Getter
    @Setter
    static class Obj8 implements Serializable {}

    @Getter
    @Setter
    static class Obj9 implements Serializable {}

    @Getter
    @Setter
    static class Obj10 implements Serializable {}

    @Getter
    @Setter
    static class Obj11 implements Serializable {}

    @Getter
    @Setter
    static class Obj12 implements Serializable {}

    @Getter
    @Setter
    static class Obj13 implements Serializable {}

    @Getter
    @Setter
    static class Obj14 implements Serializable {}

    @Getter
    @Setter
    static class Obj15 implements Serializable {}

    @Getter
    @Setter
    static class Obj16 implements Serializable {}
}
