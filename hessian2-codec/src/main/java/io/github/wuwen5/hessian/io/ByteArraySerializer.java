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

/**
 * Serializing an object for known object types.
 */
public class ByteArraySerializer extends AbstractSerializer implements ObjectSerializer {
    public static final ByteArraySerializer SER = new ByteArraySerializer();

    private ByteArraySerializer() {}

    @Override
    public HessianSerializer getObjectSerializer() {
        return this;
    }

    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
        byte[] data = (byte[]) obj;

        if (data != null) {
            out.writeBytes(data, 0, data.length);
        } else {
            out.writeNull();
        }
    }
}
