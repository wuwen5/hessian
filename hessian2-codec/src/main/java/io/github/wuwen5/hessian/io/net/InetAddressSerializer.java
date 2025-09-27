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

import io.github.wuwen5.hessian.io.AbstractHessianEncoder;
import io.github.wuwen5.hessian.io.AbstractSerializer;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * Serializing an InetAddress object.
 */
public class InetAddressSerializer extends AbstractSerializer {
    private static final InetAddressSerializer SERIALIZER = new InetAddressSerializer();

    public static InetAddressSerializer create() {
        return SERIALIZER;
    }

    @Override
    public void writeObject(Object obj, AbstractHessianEncoder out) throws IOException {

        if (out.addRef(obj)) {
            return;
        }

        InetAddress addr = (InetAddress) obj;

        Inet6Address inet6Address = null;
        final String className;
        if (obj instanceof Inet6Address) {
            inet6Address = (Inet6Address) obj;
            className = "java.net.Inet6Address";
        } else {
            className = "java.net.Inet4Address";
        }

        int ref = out.writeObjectBegin(className);

        if (ref == -1) {
            if (inet6Address != null) {
                out.writeInt(5);
                out.writeString("hostName");
                out.writeString("ipaddress");
                out.writeString("scope_id");
                out.writeString("scope_id_set");
                out.writeString("ifname");
            } else {
                out.writeInt(2);
                out.writeString("hostName");
                out.writeString("address");
            }
            out.writeObjectBegin(className);
        }

        out.writeString(addr.getHostName());

        if (inet6Address != null) {
            out.writeObject(addr.getAddress());
            out.writeInt(inet6Address.getScopeId());
            out.writeBoolean(inet6Address.getScopeId() > 0
                    || (inet6Address.getScopeId() == 0
                            && inet6Address.getHostAddress().contains("%")));
            out.writeString(
                    inet6Address.getScopedInterface() == null
                            ? null
                            : inet6Address.getScopedInterface().getName());
        } else {
            byte[] oriAddr = addr.getAddress();
            int address = ((oriAddr[0] & 0xFF) << 24)
                    | ((oriAddr[1] & 0xFF) << 16)
                    | ((oriAddr[2] & 0xFF) << 8)
                    | (oriAddr[3] & 0xFF);
            out.writeInt(address);
        }
    }
}
