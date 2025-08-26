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

import io.github.wuwen5.hessian.io.beans.BaseUser;
import io.github.wuwen5.hessian.io.beans.GrandsonUser;
import io.github.wuwen5.hessian.io.beans.SubUser;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * fix hessian serialize bug:
 * the filed of parent class will cover the filed of sub class
 */
public class HessianJavaSerializeTest extends SerializeTestBase {

    @Test
    public void testGetBaseUserName() throws Exception {

        BaseUser baseUser = new BaseUser();
        baseUser.setUserId(1);
        baseUser.setUserName("tom");

        BaseUser serializedUser = baseHessian2Serialize(baseUser);
        Assertions.assertEquals("tom", serializedUser.getUserName());
    }

    @Test
    public void testGetBaseUserNameCompact() throws Exception {

        BaseUser baseUser = new BaseUser();
        baseUser.setUserId(1);
        baseUser.setUserName("tom");

        BaseUser serializedUser = baseHessian2Serialize(baseUser);
        Assertions.assertEquals("tom", serializedUser.getUserName());
    }

    @Test
    public void testGetSubUserName() throws Exception {
        SubUser subUser = new SubUser();
        subUser.setUserId(1);
        subUser.setUserName("tom");

        // TODO
        // SubUser serializedUser = baseHessian2Serialize(subUser);
        // Assertions.assertEquals("tom", serializedUser.getUserName());
    }

    @Test
    public void testGetSubUserNameCompact() throws Exception {
        SubUser subUser = new SubUser();
        subUser.setUserId(1);
        subUser.setUserName("tom");

        SubUser serializedUser = baseHessian2Serialize(subUser);
        // TODO
        //         Assertions.assertEquals("tom", serializedUser.getUserName());
    }

    @Test
    public void testSubUserWage() throws Exception {
        SubUser subUser = new SubUser();
        subUser.setUserId(1);
        subUser.setUserName("tom");
        List<Integer> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        list.add(3);
        subUser.setAgeList(list);

        SubUser serializedUser = baseHessian2Serialize(subUser);
        Assertions.assertEquals(subUser.getAgeList(), serializedUser.getAgeList());
    }

    @Test
    public void testSubUserWageCompact() throws Exception {
        SubUser subUser = new SubUser();
        subUser.setUserId(1);
        subUser.setUserName("tom");
        List<Integer> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        list.add(3);
        subUser.setAgeList(list);

        SubUser serializedUser = baseHessian2Serialize(subUser);
        Assertions.assertEquals(subUser.getAgeList(), serializedUser.getAgeList());
    }

    /**
     * TODO
     * */
    @Test
    @Disabled
    public void testGetGrandsonUserName() throws Exception {
        GrandsonUser grandsonUser = new GrandsonUser();
        grandsonUser.setUserId(1);
        grandsonUser.setUserName("tom");

        GrandsonUser serializedUser = baseHessian2Serialize(grandsonUser);
        Assertions.assertEquals("tom", serializedUser.getUserName());
    }

    @Test
    public void testGetGrandsonUserNameCompact() throws Exception {
        GrandsonUser grandsonUser = new GrandsonUser();
        grandsonUser.setUserId(1);
        grandsonUser.setUserName("tom");

        // TODO
        // GrandsonUser serializedUser = baseHessian2Serialize(grandsonUser);
        // Assertions.assertEquals("tom", serializedUser.getUserName());
    }

    @Test
    public void testFloat() throws Exception {
        Float fData = 99.8F;
        Double dData = 99.8D;
        // TODO Why does dubbo-hessian need to be converted to double? Is it a bug in dubbo-hessian ?
        // https://github.com/apache/dubbo-hessian-lite/pull/12 ?
        Assertions.assertEquals(fData, baseHessian2Serialize(fData));
    }
}
