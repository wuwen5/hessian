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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

public class CollectionsTest extends SerializeTestBase {
    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    void testRandomAccessToUnmodifiableList() throws IOException {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        // jdk 21 TODO
        List<Integer> unmodifiableList = Collections.unmodifiableList(list);
        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList));

        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList, List.class));
    }

    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    void testRandomAccessToUnmodifiableListCompact() throws IOException {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        List<Integer> unmodifiableList = Collections.unmodifiableList(list);
        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList));
    }

    @Test
    void testLinkedToUnmodifiableList() throws IOException {
        List<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        List<Integer> unmodifiableList = Collections.unmodifiableList(list);
        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList));
    }

    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    void testLinkedToUnmodifiableListCompact() throws IOException {
        List<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        List<Integer> unmodifiableList = Collections.unmodifiableList(list);
        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList));
    }

    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    void testRandomAccessToSynchronizedList() throws IOException {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        // jdk 21 TODO
        List<Integer> unmodifiableList = Collections.synchronizedList(list);
        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList));
    }

    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    void testRandomAccessToSynchronizedListCompact() throws IOException {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        List<Integer> unmodifiableList = Collections.synchronizedList(list);
        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList));
    }

    @Test
    void testLinkedToSynchronizedList() throws IOException {
        List<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        List<Integer> unmodifiableList = Collections.synchronizedList(list);
        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList));
    }

    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    void testLinkedToSynchronizedListCompact() throws IOException {
        List<Integer> list = new LinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        List<Integer> unmodifiableList = Collections.synchronizedList(list);
        Assertions.assertEquals(unmodifiableList, baseHessian2Serialize(unmodifiableList));
    }

    @Test
    void testCopiesList() throws IOException {
        List<Integer> copiesList = Collections.nCopies(3, 1);
        Assertions.assertEquals(copiesList, baseHessian2Serialize(copiesList));
        Assertions.assertEquals(copiesList.subList(1, 2), baseHessian2Serialize(copiesList.subList(1, 2)));
    }

    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    void testCopiesListCompact() throws IOException {
        List<Integer> copiesList = Collections.nCopies(3, 1);
        Assertions.assertEquals(copiesList, baseHessian2Serialize(copiesList));

        Assertions.assertEquals(copiesList.subList(1, 2), baseHessian2Serialize(copiesList.subList(1, 2)));
    }
}
