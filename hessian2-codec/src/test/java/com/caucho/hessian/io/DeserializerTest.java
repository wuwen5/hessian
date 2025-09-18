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

package com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.AbstractHessianDecoder;
import io.github.wuwen5.hessian.io.HessianDeserializer;
import io.github.wuwen5.hessian.io.HessianProtocolException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class DeserializerTest {

    @Test
    void testDemoDeserializer() throws IOException {
        DemoDeserializer demoDeserializer = new DemoDeserializer();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (Hessian2Output output = new Hessian2Output(outputStream)) {
            output.writeObject(new Demo());
        }

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        input.setSerializerFactory(new SerializerFactory() {
            @Override
            public HessianDeserializer getDeserializer(Class<?> cl) throws HessianProtocolException {
                if (cl == Demo.class) {
                    return demoDeserializer;
                }
                return super.getDeserializer(cl);
            }
        });

        Object o = input.readObject();
        Assertions.assertEquals(Demo.class, o.getClass());
    }

    static class Demo implements Serializable {}

    static class DemoDeserializer extends AbstractDeserializer {

        @Override
        public Object readObject(AbstractHessianDecoder in, Object[] fields) {
            return new Demo();
        }
    }
}
