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

package io.github.wuwen5.hessian.io.net;

import io.github.wuwen5.hessian.io.AbstractHessianDecoder;
import io.github.wuwen5.hessian.io.BaseDeserializer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * Deserializing an InetSocketAddress object.
 */
@Slf4j
public class InetSocketAddressDeserializer extends BaseDeserializer {

    /**
     * Get the type of object that this deserializer handles.
     * @return the Class type this deserializer can handle
     */
    @Override
    public Class<?> getType() {
        return InetSocketAddress.class;
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
        String hostName = null;
        InetAddress address = null;
        int port = -1;

        int ref = in.addRef(null);

        for (Object fieldName : fields) {
            if ("hostName".equals(fieldName)) {
                hostName = in.readString();
            } else if ("addr".equals(fieldName)) {
                address = (InetAddress) in.readObject();
            } else if ("port".equals(fieldName)) {
                port = in.readInt();
            } else {
                in.readObject();
            }
        }

        InetSocketAddress obj;
        if (address != null) {
            obj = new InetSocketAddress(address, port);
        } else if (hostName != null) {
            obj = new InetSocketAddress(hostName, port);
        } else {
            obj = new InetSocketAddress(port);
        }
        in.setRef(ref, obj);

        return obj;
    }
}
