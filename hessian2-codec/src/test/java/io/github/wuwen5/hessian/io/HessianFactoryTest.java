/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.github.wuwen5.hessian.io.beans.BasicTypeBean;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HessianFactoryTest {

    private HessianFactory hessianFactory;
    private InputStream inputStream;

    @BeforeEach
    void setUp() {
        hessianFactory = new HessianFactory();
        inputStream = new ByteArrayInputStream(new byte[0]);
    }

    @Test
    void testGetSerializerFactoryReturnsNewInstanceWhenDefault() {
        Hessian2SerializerFactory original = hessianFactory.getSerializerFactory();
        Hessian2SerializerFactory second = hessianFactory.getSerializerFactory();

        assertSame(original, second);
        assertNotSame(original, Hessian2SerializerFactory.createDefault());
    }

    @Test
    void testSetAndGetSerializerFactory() {
        Hessian2SerializerFactory customFactory = new Hessian2SerializerFactory();
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
        HessianEncoder hessian2Output = hessianFactory.createHessian2Output(outputStream);
        hessian2Output.writeObject(new Obj());
        hessian2Output.flush();

        HessianDecoder input = hessianFactory.createHessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));

        Object o = input.readObject();
        assertNotNull(o);
        assertSame(Obj.class, o.getClass());

        hessianFactory.freeHessian2Input(input);
        hessianFactory.freeHessian2Output(hessian2Output);
        HessianDecoder hessian2Input =
                hessianFactory.createHessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        HessianEncoder hessian2Output2 = hessianFactory.createHessian2Output(outputStream);
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        List<String> list = new ArrayList<>();
        Consumer<String> logConsumer = list::add;
        HessianDebugOutputStream debugOutput =
                (HessianDebugOutputStream) hessianFactory.createHessian2DebugOutput(outputStream, logConsumer);

        BasicTypeBean testBean = BasicTypeBean.create();
        try (HessianEncoder output = hessianFactory.createHessian2Output(debugOutput)) {
            output.writeObject(testBean);
        }

        HessianDebugInputStream debugInputStream =
                new HessianDebugInputStream(new ByteArrayInputStream(outputStream.toByteArray()), System.out::println);

        HessianDecoder hessian2Input = hessianFactory.createHessian2Input(debugInputStream);
        Object o = hessian2Input.readObject();

        assertEquals(BasicTypeBean.class, o.getClass());
        assertEquals(testBean, o);
        assertFalse(list.isEmpty());
    }

    @Test
    void testUnsafeSerializer() throws IOException, NoSuchFieldException, IllegalAccessException {
        // UnsafeSerializer.isEnabled

        Field unsafeSerializerField = UnsafeSerializer.class.getDeclaredField("isEnabled");
        Field unsafeDeserializerField = UnsafeDeserializer.class.getDeclaredField("isEnabled");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {

            unsafeSerializerField.setAccessible(true);
            unsafeSerializerField.set(UnsafeSerializer.class, false);
            unsafeDeserializerField.setAccessible(true);
            unsafeDeserializerField.set(UnsafeDeserializer.class, false);

            List<String> list = new ArrayList<>();
            Consumer<String> logConsumer = list::add;
            HessianDebugOutputStream debugOutput =
                    (HessianDebugOutputStream) hessianFactory.createHessian2DebugOutput(outputStream, logConsumer);

            BasicTypeBean testBean = BasicTypeBean.create();
            try (HessianEncoder output = hessianFactory.createHessian2Output(debugOutput)) {
                output.writeObject(testBean);
            }

            HessianDebugInputStream debugInputStream = new HessianDebugInputStream(
                    new ByteArrayInputStream(outputStream.toByteArray()), System.out::println);

            HessianDecoder hessian2Input = hessianFactory.createHessian2Input(debugInputStream);
            Object o = hessian2Input.readObject();

            assertEquals(BasicTypeBean.class, o.getClass());
            assertEquals(testBean, o);
            assertFalse(list.isEmpty());
        } finally {
            unsafeSerializerField.set(UnsafeSerializer.class, true);
            unsafeDeserializerField.set(UnsafeDeserializer.class, true);
        }
    }

    @Test
    void testSerializerFactory() {
        com.caucho.hessian.io.SerializerFactory serializerFactory = new com.caucho.hessian.io.SerializerFactory();
        com.caucho.hessian.io.SerializerFactory serializerFactory2 = new com.caucho.hessian.io.SerializerFactory(
                Thread.currentThread().getContextClassLoader());

        assertSame(serializerFactory.getClassLoader(), serializerFactory2.getClassLoader());
    }

    static class Obj implements Serializable {}
}
