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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

public class InetAddressTest extends SerializeTestBase {
    /**
     * TODO
     * */
    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    @Disabled
    void testJdk8() throws Exception {
        Inet4Address inet4Address = (Inet4Address) Inet4Address.getByAddress("baidu.com", new byte[] {1, 2, 3, 4});
        Assertions.assertEquals(inet4Address, baseHessian2Serialize(inet4Address));
        Assertions.assertEquals(
                inet4Address.getHostAddress(),
                baseHessian2Serialize(inet4Address).getHostAddress());
        Assertions.assertEquals(
                inet4Address.getHostName(), baseHessian2Serialize(inet4Address).getHostName());
        Assertions.assertArrayEquals(
                inet4Address.getAddress(), baseHessian2Serialize(inet4Address).getAddress());

        InetAddress inetAddress = Inet6Address.getByAddress(
                "baidu.com", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        Assertions.assertEquals(inetAddress, baseHessian2Serialize(inetAddress));
        Assertions.assertEquals(
                inetAddress.getHostAddress(), baseHessian2Serialize(inetAddress).getHostAddress());
        Assertions.assertEquals(
                inetAddress.getHostName(), baseHessian2Serialize(inetAddress).getHostName());
        Assertions.assertArrayEquals(
                inetAddress.getAddress(), baseHessian2Serialize(inetAddress).getAddress());

        Inet6Address inet6Address = Inet6Address.getByAddress(
                "baidu.com", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, 10);
        Assertions.assertEquals(inet6Address, baseHessian2Serialize(inet6Address));
        Assertions.assertEquals(
                inet6Address.getHostAddress(),
                baseHessian2Serialize(inet6Address).getHostAddress());
        Assertions.assertEquals(
                inet6Address.getHostName(), baseHessian2Serialize(inet6Address).getHostName());
        Assertions.assertArrayEquals(
                inet6Address.getAddress(), baseHessian2Serialize(inet6Address).getAddress());
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_17)
    void testJdk17() throws Exception {
        Inet4Address inet4Address = (Inet4Address) Inet4Address.getByAddress("baidu.com", new byte[] {1, 2, 3, 4});
        Assertions.assertEquals(inet4Address, baseHessian2Serialize(inet4Address));
        Assertions.assertEquals(
                inet4Address.getHostAddress(),
                baseHessian2Serialize(inet4Address).getHostAddress());
        Assertions.assertEquals(
                inet4Address.getHostName(), baseHessian2Serialize(inet4Address).getHostName());
        Assertions.assertArrayEquals(
                inet4Address.getAddress(), baseHessian2Serialize(inet4Address).getAddress());

        InetAddress inetAddress = Inet6Address.getByAddress(
                "baidu.com", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        Assertions.assertEquals(inetAddress, baseHessian2Serialize(inetAddress));
        Assertions.assertEquals(
                inetAddress.getHostAddress(), baseHessian2Serialize(inetAddress).getHostAddress());
        Assertions.assertEquals(
                inetAddress.getHostName(), baseHessian2Serialize(inetAddress).getHostName());
        Assertions.assertArrayEquals(
                inetAddress.getAddress(), baseHessian2Serialize(inetAddress).getAddress());

        Inet6Address inet6Address = Inet6Address.getByAddress(
                "baidu.com", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, 10);
        Assertions.assertEquals(inet6Address, baseHessian2Serialize(inet6Address));
        // TODO
        //        Assertions.assertEquals(
        //                inet6Address.getHostAddress(),
        //                baseHessian2Serialize(inet6Address).getHostAddress());
        Assertions.assertEquals(
                inet6Address.getHostName(), baseHessian2Serialize(inet6Address).getHostName());
        Assertions.assertArrayEquals(
                inet6Address.getAddress(), baseHessian2Serialize(inet6Address).getAddress());
    }
}
