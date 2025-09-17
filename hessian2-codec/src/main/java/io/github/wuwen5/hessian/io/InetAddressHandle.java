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

import com.caucho.hessian.io.HessianHandle;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import lombok.extern.slf4j.Slf4j;

/**
 * Handle for an InetAddress object.
 */
@Slf4j
public class InetAddressHandle implements java.io.Serializable, HessianHandle {

    private String hostName;
    private byte[] address;
    private int scopeId;
    private String ifname;

    public InetAddressHandle(String hostName, byte[] address) {
        this(hostName, address, -1, null);
    }

    public InetAddressHandle(String hostName, byte[] address, int scopeId, String ifname) {
        this.hostName = hostName;
        this.address = address;
        this.scopeId = scopeId;
        this.ifname = ifname;
    }

    private Object readResolve() {
        try {
            if (ifname != null) {
                try {
                    NetworkInterface scopeIfname = NetworkInterface.getByName(ifname);
                    if (scopeIfname != null) {
                        return Inet6Address.getByAddress(this.hostName, this.address, scopeIfname);
                    }
                } catch (SocketException ignored) {
                }
            }
            if (scopeId >= 0) {
                return Inet6Address.getByAddress(this.hostName, this.address, this.scopeId);
            }
            return InetAddress.getByAddress(this.hostName, this.address);
        } catch (Exception e) {
            log.debug(e.toString(), e);

            return null;
        }
    }
}
