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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import lombok.extern.slf4j.Slf4j;

/**
 * Deserializing an InetAddress object.
 */
@Slf4j
public class InetAddressDeserializer extends BaseDeserializer {

    /**
     * The class type
     */
    private final Class<?> cls;

    public InetAddressDeserializer(Class<?> cls) {
        this.cls = cls;
    }

    /**
     * Get the type of object that this deserializer handles.
     * @return the Class type this deserializer can handle
     */
    @Override
    public Class<?> getType() {
        return cls;
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
        byte[] ipaddress = null;
        int address = -1;
        int scopeId = -1;
        String ifname = null;
        boolean scopeIdSet = false;

        for (Object fieldName : fields) {
            if ("hostName".equals(fieldName)) {
                hostName = in.readString();
            } else if ("address".equals(fieldName)) {
                address = in.readInt();
            } else if ("scope_id".equals(fieldName)) {
                scopeId = in.readInt();
            } else if ("ifname".equals(fieldName)) {
                ifname = in.readString();
            } else if ("scope_id_set".equals(fieldName)) {
                scopeIdSet = in.readBoolean();
            } else if ("ipaddress".equals(fieldName)) {
                ipaddress = (byte[]) in.readObject();
            } else {
                in.readObject();
            }
        }

        if (ifname != null) {
            try {
                NetworkInterface scopeIfname = NetworkInterface.getByName(ifname);
                if (scopeIfname != null && ipaddress != null) {
                    return addRefAndReturn(in, Inet6Address.getByAddress(hostName, ipaddress, scopeIfname));
                }
            } catch (IOException e) {
                log.debug("Failed to get network interface by name: {}", ifname, e);
            }
        }

        try {

            if (scopeIdSet && scopeId >= 0 && ipaddress != null) {
                return addRefAndReturn(in, Inet6Address.getByAddress(hostName, ipaddress, scopeId));
            }

            if (address >= 0) {
                byte[] addr = new byte[4];

                addr[0] = (byte) ((address >>> 24) & 0xFF);
                addr[1] = (byte) ((address >>> 16) & 0xFF);
                addr[2] = (byte) ((address >>> 8) & 0xFF);
                addr[3] = (byte) (address & 0xFF);
                return addRefAndReturn(in, InetAddress.getByAddress(hostName, addr));
            } else if (ipaddress != null) {
                return addRefAndReturn(in, InetAddress.getByAddress(hostName, ipaddress));
            }
        } catch (IOException e) {
            log.debug(e.toString(), e);
        }

        return null;
    }

    private static Object addRefAndReturn(AbstractHessianDecoder in, Object obj) throws IOException {
        in.addRef(obj);
        return obj;
    }
}
