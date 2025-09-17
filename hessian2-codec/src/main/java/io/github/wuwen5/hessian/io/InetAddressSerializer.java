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
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * Serializing a locale.
 */
public class InetAddressSerializer extends AbstractSerializer {
    private static InetAddressSerializer SERIALIZER = new InetAddressSerializer();

    public static InetAddressSerializer create() {
        return SERIALIZER;
    }

    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {
        if (obj == null) {
            out.writeNull();
            return;
        }
        if (out.addRef(obj)) {
            return;
        }

        InetAddress addr = (InetAddress) obj;
        if (obj instanceof Inet6Address) {
            Inet6Address inet6Address = (Inet6Address) obj;
            if (inet6Address.getScopeId() > 0 || inet6Address.getScopedInterface() != null) {
                out.writeObject(new InetAddressHandle(
                        inet6Address.getHostName(),
                        inet6Address.getAddress(),
                        inet6Address.getScopeId(),
                        inet6Address.getScopedInterface() == null
                                ? null
                                : inet6Address.getScopedInterface().getName()));
                return;
            }
        }
        out.writeObject(new InetAddressHandle(addr.getHostName(), addr.getAddress()));
    }
}
