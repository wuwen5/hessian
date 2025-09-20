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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.wuwen5.hessian.io.beans.Hessian2StringShortType;
import io.github.wuwen5.hessian.io.beans.PersonType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Hessian2StringShortTest extends SerializeTestBase {

    @Test
    public void serialize_string_short_map_then_deserialize() throws Exception {

        Hessian2StringShortType stringShort = new Hessian2StringShortType();
        Map<String, Short> stringShortMap = new HashMap<>();
        stringShortMap.put("first", (short) 0);
        stringShortMap.put("last", (short) 60);
        stringShort.stringShortMap = stringShortMap;

        Hessian2StringShortType deserialize = baseHessian2Serialize(stringShort);
        assertNotNull(deserialize.stringShortMap);
        assertEquals(2, deserialize.stringShortMap.size());
        assertInstanceOf(Short.class, deserialize.stringShortMap.get("last"));
        assertEquals(Short.valueOf((short) 0), deserialize.stringShortMap.get("first"));
        assertEquals(Short.valueOf((short) 60), deserialize.stringShortMap.get("last"));
    }

    @Test
    public void serialize_string_byte_map_then_deserialize() throws Exception {

        Hessian2StringShortType stringShort = new Hessian2StringShortType();
        Map<String, Byte> stringByteMap = new HashMap<>();
        stringByteMap.put("first", (byte) 0);
        stringByteMap.put("last", (byte) 60);
        stringShort.stringByteMap = stringByteMap;

        Hessian2StringShortType deserialize = baseHessian2Serialize(stringShort);
        assertNotNull(deserialize.stringByteMap);
        assertEquals(2, deserialize.stringByteMap.size());
        assertInstanceOf(Byte.class, deserialize.stringByteMap.get("last"));
        assertEquals(Byte.valueOf((byte) 0), deserialize.stringByteMap.get("first"));
        assertEquals(Byte.valueOf((byte) 60), deserialize.stringByteMap.get("last"));
    }

    @Test
    public void serialize_map_then_deserialize() throws Exception {

        Map<String, Short> stringShortMap = new HashMap<>();
        stringShortMap.put("first", (short) 0);
        stringShortMap.put("last", (short) 60);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        HessianEncoder out = new HessianEncoder(bout);

        out.writeObject(stringShortMap);
        out.flush();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        HessianDecoder input = new HessianDecoder(bin);
        // (Map) input.readObject(HashMap.class, String.class, Short.class); TODO
        Map deserialize = (Map) input.readObject();
        assertNotNull(deserialize);
        assertEquals(2, deserialize.size());
        assertInstanceOf(Short.class, deserialize.get("last"));
        assertEquals(Short.valueOf((short) 0), deserialize.get("first"));
        assertEquals(Short.valueOf((short) 60), deserialize.get("last"));
    }

    @Test
    public void serialize_map_then_deserialize0() throws Exception {

        Map<String, Short> stringShortMap = new HashMap<String, Short>();
        stringShortMap.put("first", (short) 0);
        stringShortMap.put("last", (short) 60);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        HessianEncoder out = new HessianEncoder(bout);

        out.writeObject(stringShortMap);
        out.flush();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        HessianDecoder input = new HessianDecoder(bin);

        //        List<Class<?>> keyValueType = new ArrayList<>();
        //        keyValueType.add(String.class);
        //        keyValueType.add(short.class);

        // (Map) input.readObject(keyValueType); TODO
        Map deserialize = (Map) input.readObject();
        assertNotNull(deserialize);
        assertEquals(2, deserialize.size());
        assertInstanceOf(Short.class, deserialize.get("last"));
        assertEquals((short) 0, deserialize.get("first"));
        assertEquals((short) 60, deserialize.get("last"));
    }

    @Test
    public void serialize_string_person_map_then_deserialize() throws Exception {

        Hessian2StringShortType stringShort = new Hessian2StringShortType();
        Map<String, PersonType> stringPersonTypeMap = new HashMap<>();
        stringPersonTypeMap.put(
                "first",
                new PersonType(
                        "jason.shang", 26, (double) 0.1, (short) 1, (byte) 2, Arrays.asList((short) 1, (short) 1)));
        stringPersonTypeMap.put(
                "last",
                new PersonType(
                        "jason.shang2", 52, (double) 0.2, (short) 2, (byte) 4, Arrays.asList((short) 2, (short) 2)));
        stringShort.stringPersonTypeMap = stringPersonTypeMap;

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        HessianEncoder out = new HessianEncoder(bout);

        out.writeObject(stringShort);
        out.flush();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        HessianDecoder input = new HessianDecoder(bin);

        Hessian2StringShortType deserialize = (Hessian2StringShortType) input.readObject();
        assertNotNull(deserialize.stringPersonTypeMap);
        assertEquals(2, deserialize.stringPersonTypeMap.size());
        assertInstanceOf(PersonType.class, deserialize.stringPersonTypeMap.get("last"));

        assertEquals(
                new PersonType(
                        "jason.shang", 26, (double) 0.1, (short) 1, (byte) 2, Arrays.asList((short) 1, (short) 1)),
                deserialize.stringPersonTypeMap.get("first"));

        assertEquals(
                new PersonType(
                        "jason.shang2", 52, (double) 0.2, (short) 2, (byte) 4, Arrays.asList((short) 2, (short) 2)),
                deserialize.stringPersonTypeMap.get("last"));
    }

    @Test
    public void serialize_list_then_deserialize() throws Exception {

        List<Short> shortList = new ArrayList<Short>();
        shortList.add((short) 0);
        shortList.add((short) 60);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        HessianEncoder out = new HessianEncoder(bout);

        out.writeObject(shortList);
        out.flush();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        HessianDecoder input = new HessianDecoder(bin);
        // (List) input.readObject(ArrayList.class, Short.class); TODO
        List<Short> deserialize = (List) input.readObject();
        assertNotNull(deserialize);
        assertEquals(2, deserialize.size());
        assertInstanceOf(Short.class, deserialize.get(1));
        assertEquals(Short.valueOf((short) 0), deserialize.get(0));
        assertEquals(Short.valueOf((short) 60), deserialize.get(1));
    }

    @Test
    public void serialize_list_then_deserialize0() throws Exception {

        List<Short> shortList = new ArrayList<>();
        shortList.add((short) 0);
        shortList.add((short) 60);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        HessianEncoder out = new HessianEncoder(bout);

        out.writeObject(shortList);
        out.flush();

        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        HessianDecoder input = new HessianDecoder(bin);

        List<Class<?>> valueType = new ArrayList<>();
        valueType.add(short.class);

        // (List) input.readObject(valueType); TODO
        List<Short> deserialize = (List) input.readObject();
        assertNotNull(deserialize);
        assertEquals(2, deserialize.size());
        assertInstanceOf(Short.class, deserialize.get(1));
        assertEquals(Short.valueOf((short) 0), deserialize.get(0));
        assertEquals(Short.valueOf((short) 60), deserialize.get(1));
    }

    @Test
    public void serialize_short_set_then_deserialize() throws Exception {

        Hessian2StringShortType stringShort = new Hessian2StringShortType();
        Set<Short> shortSet = new HashSet<>();
        shortSet.add((short) 0);
        shortSet.add((short) 60);
        stringShort.shortSet = shortSet;

        Hessian2StringShortType deserialize = baseHessian2Serialize(stringShort);
        assertNotNull(deserialize.shortSet);
        assertEquals(2, deserialize.shortSet.size());
        assertTrue(deserialize.shortSet.contains((short) 0));
        assertTrue(deserialize.shortSet.contains((short) 60));
    }

    @Test
    public void test_string_short_type() throws IOException {
        for (int i = 0; i < 100; i++) {
            Hessian2StringShortType obj = new Hessian2StringShortType();
            obj.shortSet = new HashSet<>();
            obj.stringShortMap = new HashMap<>();
            obj.stringByteMap = new HashMap<>();
            obj.stringPersonTypeMap = new HashMap<>();

            obj.shortSet.add((short) i);
            obj.shortSet.add((short) (i * 2));

            obj.stringShortMap.put(String.valueOf(i), (short) i);
            obj.stringShortMap.put(String.valueOf(i * 100), (short) (i * 100));
            obj.stringByteMap.put(String.valueOf(i), (byte) 1);

            List<Short> shorts = Arrays.asList((short) 12, (short) 4);
            PersonType abc = new PersonType("ABC", 12, 128D, (short) 1, (byte) 2, shorts);
            obj.stringPersonTypeMap.put("P_" + i, abc);

            Hessian2StringShortType newObj = baseHessian2Serialize(obj);
            Assertions.assertEquals(obj, newObj);
            System.out.println("ShortTypeTest.testHessian2StringShortType(): i=" + i + " passed!");
        }
    }
}
