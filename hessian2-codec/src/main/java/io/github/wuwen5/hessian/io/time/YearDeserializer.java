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

package io.github.wuwen5.hessian.io.time;

import com.caucho.hessian.io.AbstractDeserializer;
import io.github.wuwen5.hessian.io.AbstractHessianDecoder;
import java.io.IOException;
import java.time.Year;

/**
 * @author wuwen
 */
public class YearDeserializer extends AbstractDeserializer {

    /**
     * Get the type of object that this deserializer
     * @return the type of object that this deserializer
     */
    @Override
    public Class<?> getType() {
        return Year.class;
    }

    /**
     * Read the object from the input stream.
     * @param in the deserializer
     * @param fields the field names
     * @return the deserialized object
     * @throws IOException if an I/O error occurs
     */
    @Override
    public Object readObject(AbstractHessianDecoder in, Object[] fields) throws IOException {
        Object obj = Year.of(in.readInt());
        in.addRef(obj);
        return obj;
    }
}
