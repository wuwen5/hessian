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
import java.text.NumberFormat;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

public class NumberFormatTest extends SerializeTestBase {
    @Test
    void test() throws IOException {
        NumberFormat originalNumberFormat = NumberFormat.getInstance(Locale.US);

        NumberFormat result = baseHessian2Serialize(originalNumberFormat);

        Assertions.assertEquals(originalNumberFormat.getMaximumFractionDigits(), result.getMaximumFractionDigits());
        // TODO
        //        Assertions.assertEquals(originalNumberFormat.getMaximumIntegerDigits(),
        // result.getMaximumIntegerDigits());
        Assertions.assertEquals(originalNumberFormat.getMinimumFractionDigits(), result.getMinimumFractionDigits());
        Assertions.assertEquals(originalNumberFormat.getMinimumIntegerDigits(), result.getMinimumIntegerDigits());
        Assertions.assertEquals(originalNumberFormat.getRoundingMode(), result.getRoundingMode());
        // TODO Support currency
        //        Assertions.assertEquals(originalNumberFormat.getCurrency(), result.getCurrency());
    }

    @Test
    @EnabledForJreRange(max = JRE.JAVA_11)
    void testCompact() throws IOException {
        NumberFormat obj = NumberFormat.getInstance(Locale.US);
        // TODO
        //        Assertions.assertEquals(
        //                obj.getMaximumFractionDigits(), baseHessian2Serialize(obj).getMaximumFractionDigits());
        //        Assertions.assertEquals(
        //                obj.getMaximumIntegerDigits(), baseHessian2Serialize(obj).getMaximumIntegerDigits());
        //        Assertions.assertEquals(
        //                obj.getMinimumFractionDigits(), baseHessian2Serialize(obj).getMinimumFractionDigits());
        //        Assertions.assertEquals(
        //                obj.getMinimumIntegerDigits(), baseHessian2Serialize(obj).getMinimumIntegerDigits());
        //        Assertions.assertEquals(
        //                obj.getRoundingMode(), baseHessian2Serialize(obj).getRoundingMode());
        // TODO Support currency
        //        Assertions.assertEquals(obj.getCurrency(), baseHessian2Serialize(obj).getCurrency());

        // TODO Support currency
        //        Assertions.assertEquals(obj.getCurrency(), hessian3ToHessian3(obj).getCurrency());

        // TODO Support currency
        //        Assertions.assertEquals(obj.getCurrency(), hessian4ToHessian3(obj).getCurrency());

        // TODO Support currency
        //        Assertions.assertEquals(obj.getCurrency(), hessian3ToHessian4(obj).getCurrency());
    }
}
