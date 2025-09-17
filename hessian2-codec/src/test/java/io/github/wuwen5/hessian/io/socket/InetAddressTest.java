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
package io.github.wuwen5.hessian.io.socket;

import io.github.wuwen5.hessian.io.SerializeTestBase;
import io.vavr.control.Try;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InetAddressTest extends SerializeTestBase {

    /**
     * */
    @Test
    void testInetAddress() throws Exception {
        Inet4Address inet4Address = (Inet4Address) Inet4Address.getByAddress("baidu.com", new byte[] {1, 2, 3, 4});
        Inet4Address serialize = baseHessian2Serialize(inet4Address);
        Assertions.assertEquals(inet4Address, serialize);
        Assertions.assertEquals(inet4Address.getHostAddress(), serialize.getHostAddress());
        Assertions.assertEquals(inet4Address.getHostName(), serialize.getHostName());
        Assertions.assertArrayEquals(inet4Address.getAddress(), serialize.getAddress());

        InetAddress inetAddress = Inet6Address.getByAddress(
                "baidu.com", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        InetAddress inetAddress1 = baseHessian2Serialize(inetAddress);
        Assertions.assertEquals(inetAddress, inetAddress1);
        Assertions.assertEquals(inetAddress.getHostAddress(), inetAddress1.getHostAddress());
        Assertions.assertEquals(inetAddress.getHostName(), inetAddress1.getHostName());
        Assertions.assertArrayEquals(inetAddress.getAddress(), inetAddress1.getAddress());

        Inet6Address inet6Address = Inet6Address.getByAddress(
                "baidu.com", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, 10);
        Inet6Address inet6Address1 = baseHessian2Serialize(inet6Address);
        Assertions.assertEquals(inet6Address, inet6Address1);
        Assertions.assertEquals(inet6Address.getHostAddress(), inet6Address1.getHostAddress());
        Assertions.assertEquals(inet6Address.getHostName(), inet6Address1.getHostName());
        Assertions.assertArrayEquals(inet6Address.getAddress(), inet6Address1.getAddress());
    }

    @Test
    void testGetByAddressWithNetworkInterface() throws Exception {
        Optional<NetworkInterface> optNif = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .filter(t -> Try.of(t::isUp).get())
                .filter(nif -> Collections.list(nif.getInetAddresses()).stream()
                        .anyMatch(address -> address instanceof Inet6Address))
                .findFirst();

        if (optNif.isPresent()) {
            NetworkInterface nif = optNif.get();
            Inet6Address inet6Address = Inet6Address.getByAddress(
                    "baidu.com", InetAddress.getByName("::1").getAddress(), nif);
            Inet6Address result = baseHessian2Serialize(inet6Address);
            Assertions.assertEquals(inet6Address, result);
            Assertions.assertEquals(inet6Address.getHostAddress(), result.getHostAddress());
            Assertions.assertEquals(inet6Address.getHostName(), result.getHostName());
            Assertions.assertArrayEquals(inet6Address.getAddress(), result.getAddress());
        }
    }
}
