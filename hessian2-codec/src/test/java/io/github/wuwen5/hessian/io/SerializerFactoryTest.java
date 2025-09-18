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

import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializerFactoryTest {

    private static final int THREADS = 50;

    @Test
    public void getSerializer() throws Exception {
        final Hessian2SerializerFactory serializerFactory = new Hessian2SerializerFactory();
        final Class<TestClass> klass = TestClass.class;

        HessianSerializer s1 = serializerFactory.getSerializer(klass);
        HessianSerializer s2 = serializerFactory.getSerializer(klass);

        Assertions.assertSame(s1, s2, "serveral Serializer!");
    }

    @Test
    public void getSerializerDuplicateThread() throws Exception {
        final Hessian2SerializerFactory serializerFactory = new Hessian2SerializerFactory();
        final Class<TestClass> klass = TestClass.class;
        final CountDownLatch countDownLatch = new CountDownLatch(THREADS);

        // init into cached map
        final HessianSerializer s = serializerFactory.getSerializer(klass);

        // get from duplicate thread
        for (int i = 0; i < THREADS; i++) {
            new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Assertions.assertSame(
                                        s, serializerFactory.getSerializer(klass), "serveral Serializer!");
                            } catch (HessianProtocolException e) {
                                e.printStackTrace();
                            }
                            countDownLatch.countDown();
                        }
                    })
                    .start();
        }
        countDownLatch.await();
    }

    @Test
    public void getDeserializer() throws Exception {
        final Hessian2SerializerFactory serializerFactory = new Hessian2SerializerFactory();
        final Class<TestClass> klass = TestClass.class;

        HessianDeserializer d1 = serializerFactory.getDeserializer(klass);
        HessianDeserializer d2 = serializerFactory.getDeserializer(klass);

        Assertions.assertSame(d1, d2, "several Deserializer!");
    }

    @Test
    public void testCheckSerializable() throws HessianProtocolException {
        final Hessian2SerializerFactory serializerFactory = new Hessian2SerializerFactory();
        try {
            serializerFactory.getSerializer(TestImpl.class);
            Assertions.fail();
        } catch (RuntimeException e) {
            Assertions.assertEquals(IllegalStateException.class, e.getClass());
            Assertions.assertEquals(
                    e.getMessage(),
                    "Serialized class " + TestImpl.class.getName() + " must implement java.io.Serializable");
        }

        //        try {
        //            serializerFactory.getDeserializer(TestImpl.class);
        //            Assertions.fail();
        //        } catch (RuntimeException e) {
        //            Assertions.assertEquals(IllegalStateException.class, e.getClass());
        //            Assertions.assertTrue(
        //                    e.getMessage()
        //                            .startsWith(
        //                                    "Serialized class com.alibaba.com.caucho.hessian.io.TestImpl must
        // implement java.io.Serializable"));
        //        }

        Assertions.assertNotNull(serializerFactory.getSerializer(TestClass.class));
        Assertions.assertNotNull(serializerFactory.getDeserializer(TestClass.class));
    }

    @Test
    public void getDeserializerDuplicateThread() throws Exception {
        final Hessian2SerializerFactory serializerFactory = new Hessian2SerializerFactory();
        final Class<TestClass> klass = TestClass.class;
        final CountDownLatch countDownLatch = new CountDownLatch(THREADS);

        // init into cached map
        final HessianDeserializer s = serializerFactory.getDeserializer(klass);

        // get from duplicate thread
        for (int i = 0; i < THREADS; i++) {
            new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Assertions.assertSame(
                                        s, serializerFactory.getDeserializer(klass), "serveral Deserializer!");
                            } catch (HessianProtocolException e) {
                                e.printStackTrace();
                            }
                            countDownLatch.countDown();
                        }
                    })
                    .start();
        }
        countDownLatch.await();
    }

    @Test
    public void getDeserializerByType() throws Exception {
        final Hessian2SerializerFactory serializerFactory = new Hessian2SerializerFactory();

        final String testClassName = TestClass.class.getName();
        HessianDeserializer d1 = serializerFactory.getDeserializer(testClassName);
        Assertions.assertNotNull(d1, "TestClass Deserializer!");

        HessianDeserializer d2 = serializerFactory.getDeserializer("com.test.NotExistClass");
        Assertions.assertNull(d2, "NotExistClass Deserializer!");
        // again check NotExistClass, there should be no warning like Hessian/Burlap:.....
        HessianDeserializer d3 = serializerFactory.getDeserializer("com.test.NotExistClass");
        Assertions.assertNull(d3, "NotExistClass Deserializer!");
    }
}
