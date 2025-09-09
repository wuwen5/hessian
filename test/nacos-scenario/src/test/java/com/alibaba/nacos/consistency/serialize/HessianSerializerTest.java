/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.alibaba.nacos.consistency.serialize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;
import org.apache.http.HttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link HessianSerializer} unit test.
 */
class HessianSerializerTest {

    private HessianSerializer hessianSerializer;

    @BeforeEach
    void setUp() {
        hessianSerializer = new HessianSerializer();
    }

    @Test
    void testSerializerAndDeserialize() {
        String data = "xxx";
        byte[] bytes = hessianSerializer.serialize(data);

        try {
            hessianSerializer.deserialize(bytes);
        } catch (Exception e) {
            assertInstanceOf(RuntimeException.class, e);
        }

        String res1 = hessianSerializer.deserialize(bytes, String.class);
        assertEquals(data, res1);

        String res2 = hessianSerializer.deserialize(bytes, "java.lang.String");
        assertEquals(data, res2);
    }

    @Test
    void testSerializerAndDeserializeForNotAllowClass() {
        Serializable data = new HttpException();
        byte[] bytes = hessianSerializer.serialize(data);

        try {
            HttpException res = hessianSerializer.deserialize(bytes);
            fail("deserialize success which is not expected");
        } catch (Exception e) {
            assertInstanceOf(ClassCastException.class, e);
        }

        try {
            HttpException res1 = hessianSerializer.deserialize(bytes, HttpException.class);
        } catch (Exception e) {
            assertInstanceOf(IllegalStateException.class, e);
        }
    }

    @Test
    void testName() {
        assertEquals("Hessian", hessianSerializer.name());
    }
}
