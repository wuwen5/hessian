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
import java.math.BigInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BigIntegerTest extends SerializeTestBase {
    @Test
    void test() throws IOException {
        BigInteger originalBigInteger = new BigInteger("1234567890");

        BigInteger result = baseHessian2Serialize(originalBigInteger);

        Assertions.assertEquals(originalBigInteger, result);
    }

    @Test
    void testWithUnsafeDisabled() throws IOException {
        String originalUnsafeProp = System.getProperty("com.caucho.hessian.unsafe");
        try {
            System.setProperty("com.caucho.hessian.unsafe", "false");

            BigInteger originalBigInteger = new BigInteger("1234567890");
            BigInteger result = baseHessian2Serialize(originalBigInteger);
            Assertions.assertEquals(originalBigInteger, result);

            // Test with larger value
            BigInteger largeBigInteger = new BigInteger("123456789012345678901234567890");
            BigInteger largeResult = baseHessian2Serialize(largeBigInteger);
            Assertions.assertEquals(largeBigInteger, largeResult);

            // Test with negative value
            BigInteger negativeBigInteger = new BigInteger("-9876543210");
            BigInteger negativeResult = baseHessian2Serialize(negativeBigInteger);
            Assertions.assertEquals(negativeBigInteger, negativeResult);
        } finally {
            if (originalUnsafeProp != null) {
                System.setProperty("com.caucho.hessian.unsafe", originalUnsafeProp);
            } else {
                System.clearProperty("com.caucho.hessian.unsafe");
            }
        }
    }
}
