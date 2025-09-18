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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.caucho.hessian.io.AbstractDeserializer;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ContextSerializerFactoryTest {

    static class MyPojo {
        String name;
    }

    static class MyPojoHessianSerializer extends AbstractSerializer {
        @Override
        public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
            MyPojo pojo = (MyPojo) obj;
            out.writeString(pojo.name);
        }
    }

    static class MyPojoHessianDeserializer extends AbstractDeserializer {
        @Override
        public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
            MyPojo pojo = new MyPojo();
            pojo.name = in.readString();
            return pojo;
        }
    }

    @Test
    void testGetCustomSerializer() {
        ContextSerializerFactory contextFactory = ContextSerializerFactory.create();
        HessianSerializer serializer = contextFactory.getCustomSerializer(MyPojo.class);
        HessianDeserializer deserializer = contextFactory.getCustomDeserializer(MyPojo.class);
        assertNotNull(serializer);
        assertNotNull(deserializer);
        assertInstanceOf(MyPojoHessianSerializer.class, serializer);
        assertInstanceOf(MyPojoHessianDeserializer.class, deserializer);
    }
}
