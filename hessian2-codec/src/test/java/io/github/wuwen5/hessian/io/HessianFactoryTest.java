package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HessianFactoryTest {

    private HessianFactory hessianFactory;
    private InputStream inputStream;
    private OutputStream outputStream;

    @BeforeEach
    void setUp() {
        hessianFactory = new HessianFactory();
        inputStream = new ByteArrayInputStream(new byte[0]);
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    void testGetSerializerFactoryReturnsNewInstanceWhenDefault() {
        SerializerFactory original = hessianFactory.getSerializerFactory();
        SerializerFactory second = hessianFactory.getSerializerFactory();

        assertSame(original, second);
        assertNotSame(original, SerializerFactory.createDefault());
    }

    @Test
    void testSetAndGetSerializerFactory() {
        SerializerFactory customFactory = new SerializerFactory();
        hessianFactory.setSerializerFactory(customFactory);

        assertSame(customFactory, hessianFactory.getSerializerFactory());
    }

    @Test
    void testSetWhitelist() throws ClassNotFoundException {
        Class<?> aClass = hessianFactory.getSerializerFactory().loadSerializedClass(Obj.class.getName());
        assertEquals(Obj.class, aClass);

        hessianFactory.setWhitelist(true);
        aClass = hessianFactory.getSerializerFactory().loadSerializedClass(Obj.class.getName());
        assertEquals(HashMap.class, aClass);
    }

    @Test
    void testAllow() throws ClassNotFoundException {
        String pattern = "io.github.wuwen5.hessian.io.*";
        hessianFactory.allow(pattern);

        hessianFactory.setWhitelist(true);
        Class<?> aClass = hessianFactory.getSerializerFactory().loadSerializedClass(Obj.class.getName());
        assertEquals(Obj.class, aClass);
    }

    @Test
    void testDeny() throws ClassNotFoundException {
        hessianFactory.deny(Obj.class.getName());
        Class<?> aClass = hessianFactory.getSerializerFactory().loadSerializedClass(Obj.class.getName());
        assertEquals(HashMap.class, aClass);
    }

    @Test
    void testCreateHessian2Input() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = hessianFactory.createHessian2Output(outputStream);
        hessian2Output.writeObject(new Obj());
        hessian2Output.flush();

        Hessian2Input input = hessianFactory.createHessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));

        Object o = input.readObject();
        assertNotNull(o);
        assertSame(Obj.class, o.getClass());

        hessianFactory.freeHessian2Input(input);
        hessianFactory.freeHessian2Output(hessian2Output);
        Hessian2Input hessian2Input =
                hessianFactory.createHessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        Hessian2Output hessian2Output2 = hessianFactory.createHessian2Output(outputStream);
        assertSame(input, hessian2Input);
        assertSame(hessian2Output, hessian2Output2);
    }

    @Test
    void testFreeHessian2IOWithNull() {
        assertDoesNotThrow(() -> hessianFactory.freeHessian2Input(null));
        assertDoesNotThrow(() -> hessianFactory.freeHessian2Output(null));
        assertDoesNotThrow(() -> hessianFactory.freeHessian2StreamingOutput(null));
    }

    @Test
    void testCreateHessian2DebugOutput() throws IOException {
        List<String> list = new ArrayList<>();
        Consumer<String> logConsumer = list::add;
        HessianDebugOutputStream debugOutput =
                (HessianDebugOutputStream) hessianFactory.createHessian2DebugOutput(outputStream, logConsumer);

        debugOutput.write('T');
        debugOutput.write('F');
        debugOutput.close();

        assertEquals(2, list.size());
    }

    static class Obj implements Serializable {}
}
