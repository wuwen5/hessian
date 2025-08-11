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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

public class InetSocketAddressTest extends SerializeTestBase {
    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    @Disabled
    void testJdk8() throws Exception {
        // TODO fix me
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 8080);
        Assertions.assertEquals(inetSocketAddress, baseHessian2Serialize(inetSocketAddress));
        Assertions.assertEquals(
                inetSocketAddress.getAddress(),
                baseHessian2Serialize(inetSocketAddress).getAddress());
        Assertions.assertEquals(
                inetSocketAddress.getHostName(),
                baseHessian2Serialize(inetSocketAddress).getHostName());
        Assertions.assertEquals(
                inetSocketAddress.getHostString(),
                baseHessian2Serialize(inetSocketAddress).getHostString());

        Assertions.assertEquals(
                inetSocketAddress.getPort(),
                baseHessian2Serialize(inetSocketAddress).getPort());

        inetSocketAddress = new InetSocketAddress("unknown.host", 8080);
        Assertions.assertEquals(inetSocketAddress, baseHessian2Serialize(inetSocketAddress));
        Assertions.assertEquals(
                inetSocketAddress.getAddress(),
                baseHessian2Serialize(inetSocketAddress).getAddress());
        Assertions.assertEquals(
                inetSocketAddress.getHostName(),
                baseHessian2Serialize(inetSocketAddress).getHostName());
        Assertions.assertEquals(
                inetSocketAddress.getHostString(),
                baseHessian2Serialize(inetSocketAddress).getHostString());

        Assertions.assertEquals(
                inetSocketAddress.getPort(),
                baseHessian2Serialize(inetSocketAddress).getPort());

        InetAddress inet6Address = Inet6Address.getByAddress(
                "baidu.com", new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        inetSocketAddress = new InetSocketAddress(inet6Address, 8080);
        Assertions.assertEquals(inetSocketAddress, baseHessian2Serialize(inetSocketAddress));
        Assertions.assertEquals(
                inetSocketAddress.getAddress(),
                baseHessian2Serialize(inetSocketAddress).getAddress());
        Assertions.assertEquals(
                inetSocketAddress.getHostName(),
                baseHessian2Serialize(inetSocketAddress).getHostName());
        Assertions.assertEquals(
                inetSocketAddress.getHostString(),
                baseHessian2Serialize(inetSocketAddress).getHostString());
        Assertions.assertEquals(
                inetSocketAddress.getPort(),
                baseHessian2Serialize(inetSocketAddress).getPort());
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_17)
    void testJdk17() throws Exception {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 8080);
        Assertions.assertEquals(inetSocketAddress, baseHessian2Serialize(inetSocketAddress));
    }
}
